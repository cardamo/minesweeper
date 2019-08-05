package minesweeper

object Visualizer {
  def printMap(map: GameModel, showMines: Boolean = false): String = {
    val result = new StringBuilder("\n")
    if (map.isWin) result.append("    Congratulations! You won!\n")
    else if (map.isFailure) result.append("    WASTED\n")

    result.append("  x  ")
    0.until(map.cols).foreach(x => result.append(x.toString.padTo(3, ' ')))
    result.append('\n')
    result.append("y   ").append("---" * map.cols)
    result.append('\n')
    0.until(map.rows).foreach(y => {
      0.until(map.cols).foreach(x => {
        if (x == 0)
          result.append(y.toString.padTo(3, ' ')).append("|")

        val cell = (x, y)
        val symbol: String =
          if (map.opened.contains(cell))
            if (map.mines.contains(cell)) Symbols.explode else map.minesAround(cell).toString
          else if (map.flags.contains(cell)) Symbols.flag
          else if (map.mines.contains(cell))
            if (showMines) Symbols.mine else Symbols.empty
          else Symbols.empty

        result.append(s" $symbol ")

      })
      result.append('\n')
    })

    result.toString()
  }
}
