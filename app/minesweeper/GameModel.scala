package minesweeper

import minesweeper.Commands.{ActionCommand, FlagCell, OpenCell}
import minesweeper.GameModel.{Cell, randomizeCells}

import scala.collection.mutable
import scala.util.Random

object GameModel {

  sealed trait CommandResult
  case class CommandFailed(reason: String) extends CommandResult
  case class CommandSucceed(newState: GameModel) extends CommandResult

  def handleCommand(map: GameModel, cmd: ActionCommand): CommandResult = {
    if (map.isComplete)
      return CommandFailed("Game is complete")

    val validation = cmd match {
      case FlagCell(x, y) => Validations.validateFlag((x, y), map)
      case OpenCell(x, y) => Validations.validateOpen((x, y), map)
    }

    validation match {
      case Some(error) => CommandFailed(error)
      case _ => cmd match {
        case FlagCell(x, y) => CommandSucceed(map.copy(flags = map.flags + ((x, y))))
        case OpenCell(x, y) => CommandSucceed(map.withOpened((x, y)))
        case _ => CommandFailed("This is very unknown command!")
      }
    }
  }

  type Cell = (Int, Int)

  def randomizeCells(x: Int, y: Int, count: Int): Set[Cell] =
    Random.shuffle(0.until(x * y).toList)
      .take(count)
      .map(n => (n / y, n % y))
      .toSet
}

case class GameModel(
  cols: Int, rows: Int,
  mines: Set[Cell] = Set(),
  flags: Set[Cell] = Set(),
  opened: Set[Cell] = Set(),
) {

  def withRandomMines(minesCount: Int): GameModel = copy(mines = randomizeCells(cols, rows, minesCount))

  def withOpened(cell: Cell): GameModel = {

    val bulkOpened = mutable.TreeSet[Cell](cell)

    if (!mines.contains(cell)) {
      val queue = mutable.Queue(cell)
      while (queue.nonEmpty) {
        val c = queue.dequeue()
        val around = cellsAround(c)

        if (!around.exists(mines.contains)) {
          queue.enqueueAll(around.removedAll(bulkOpened))
          bulkOpened.addAll(around)
        }
      }
    }

    copy(opened = opened ++ bulkOpened)
  }

  def cellsAround(cell: Cell): Set[Cell] = cell match {
    case (x, y) => Set(
      (x - 1, y - 1), (x + 0, y - 1), (x + 1, y - 1),
      (x - 1, y + 0), /*           */ (x + 1, y + 0),
      (x - 1, y + 1), (x + 0, y + 1), (x + 1, y + 1)
    ).filter({
      case (c, r) => c >= 0 && r >= 0 && c < this.cols && r < this.rows
    })
  }

  def minesAround(cell: Cell): Int = cellsAround(cell).count(mines.contains)

  def isFailure: Boolean = mines.intersect(opened).nonEmpty

  def isWin: Boolean = opened.size == (cols * rows - mines.size) && !isFailure

  def isComplete: Boolean = isFailure || isWin
}

object Validations {

  def validateMap(cols: Int, rows: Int, mines: Int): Option[String] =
    if (cols < 1 || cols > 99 || rows < 1 || rows > 99)
      Some("Fields dimensions should be within [1, 99]")
    else if (cols * rows <= mines)
      Some("Too many mines")
    else if (mines < 1)
      Some("Too few mines")
    else None

  // TODO: deduplicate validations
  def validateFlag(cell: Cell, map: GameModel): Option[String] =
    if (cell._1 >= map.cols || cell._2 >= map.rows || cell._1 < 0 || cell._2 < 0)
      Some("Invalid cell")
    else if (map.flags.contains(cell))
      Some("Already flagged")
    else if (map.opened.contains(cell))
      Some("Already opened")
    else
      None

  def validateOpen(cell: Cell, map: GameModel): Option[String] =
    if (cell._1 >= map.cols || cell._2 >= map.rows || cell._1 < 0 || cell._2 < 0)
      Some("Invalid cell")
    else if (map.opened.contains(cell))
      Some("Already opened")
    else
      None
}