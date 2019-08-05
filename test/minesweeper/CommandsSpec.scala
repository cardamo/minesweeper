package minesweeper

import minesweeper.Commands.parseCommand
import org.specs2.mutable._


class CommandsSpec extends Specification {
  "Commands Parser " should {
    "parse 'x,y' as Open command" in {
      parseCommand("3,6") should equalTo(Commands.OpenCell(3, 6))
    }

    "parse 'flag CxR as Flag command" in {
      parseCommand("flag 1x5") should equalTo(Commands.FlagCell(1, 5))
    }

    "parse New command with parameters" in {
      parseCommand("NEW 3 2 1") should equalTo(Commands.NewMap(3, 2, 1))
    }
  }

  "Open command" should {
    "open adjacent safe spots" in {
      val initial = GameModel(3, 3, mines = Set((2, 2)))
      val withOpened = initial.withOpened((0, 0))
      withOpened.opened should equalTo(Set(
        (0, 0), (0, 1), (0, 2),
        (1, 0), (1, 1), (1, 2),
        (2, 0), (2, 1),
      ))
    }

    "should end a game when opening a mine" in {
      GameModel(2, 2, mines = Set((0, 0)))
        .withOpened((0, 0))
        .isFailure should beTrue
    }
  }
}
