package minesweeper

import scala.util.matching.Regex

object Commands {

  sealed trait Command
  sealed trait ActionCommand extends Command

  case class Unknown(source: String) extends Command
  case class NewMap(cols: Int, rows: Int, mines: Int) extends Command

  case class FlagCell(x: Int, y: Int) extends ActionCommand
  case class OpenCell(x: Int, y: Int) extends ActionCommand

  private val newPattern: Regex = "new (\\d+)[., x](\\d+) (\\d+)".r
  private val openPattern: Regex = "(?:open )?(\\d+)[., x](\\d+)".r
  private val flagPattern: Regex = "flag (\\d+)[., x](\\d+)".r

  val defaultNew = Commands.NewMap(10, 10, 20)

  def parseCommand(str: String): Command = str.toLowerCase match {
    case "new" => defaultNew
    case newPattern(w, h, mines) => NewMap(w.toInt, h.toInt, mines.toInt)
    case openPattern(x, y) => OpenCell(x.toInt, y.toInt)
    case flagPattern(x, y) => FlagCell(x.toInt, y.toInt)
    case _ => Unknown(str.toString)
  }
}
