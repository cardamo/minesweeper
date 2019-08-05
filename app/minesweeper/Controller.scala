package minesweeper

import akka.actor.{Actor, ActorRef, ActorSystem, Props}
import akka.stream.Materializer
import akka.stream.scaladsl.Source
import javax.inject._
import minesweeper.Commands.{ActionCommand, NewMap}
import play.api.Configuration
import play.api.libs.streams.ActorFlow
import play.api.mvc._

import scala.concurrent.duration._

class Controller @Inject()(cc: ControllerComponents, config: Configuration)(system: ActorSystem, mat: Materializer)
  extends AbstractController(cc) {

  def socket: WebSocket = WebSocket.accept[String, String](_ => {
    val heartbeatInterval = config.get[FiniteDuration]("heartbeat.interval")
    val heartbeat = Source.tick(heartbeatInterval, heartbeatInterval, Symbols.heart)
    val actorFlow = ActorFlow.actorRef(out => GameSession.props(out))(system, mat)
    actorFlow.merge(heartbeat)
  })
}

object GameSession {
  def props(out: ActorRef) = Props(new GameSession(out))
}

class GameSession(out: ActorRef) extends Actor {

  private val unsupportedCommandResponse = "Can't handle this command. Please use `help`"

  private val donePlayingHelp =
    """
      | Current game is completed.
      |   `map`                       # show the current map with all mines revealed
      |   `new [$cols $rows $mines]`  # start a new game
    """.stripMargin

  private val playingHelp =
    """
      | Available commands:
      |   `map`                       # show the current map
      |   `new [$cols $rows $mines]`  # start a new game
      |   `[open] $column $row`       # to open a cell (left click)
      |   `flag $column $row`         # flag a cell that you think contains a mine (right click)
    """.stripMargin

  private def generateMap(config: Commands.NewMap) = {
    GameModel(config.cols, config.rows).withRandomMines(config.mines)
  }

  private def donePlaying(map: GameModel): Receive = {
    case "" =>
    case "map" => out ! Visualizer.printMap(map, showMines = true)
    case "help" => out ! donePlayingHelp
    case msg: String => Commands.parseCommand(msg) match {
      case config: NewMap => newGame(config)
      case _ => out ! unsupportedCommandResponse
    }
  }

  private def playing(map: GameModel): Receive = {
    case "" =>
    case "map" => out ! Visualizer.printMap(map, showMines = map.isComplete)
    case "help" => out ! playingHelp
    case "what is the matrix" => out ! "Cheater!!!\n" + Visualizer.printMap(map, showMines = true)
    case msg: String => Commands.parseCommand(msg) match {
      case config: NewMap => newGame(config)
      case cmd: ActionCommand => GameModel.handleCommand(map, cmd) match {
        case GameModel.CommandFailed(reason) => out ! reason + "\n" + Visualizer.printMap(map)
        case GameModel.CommandSucceed(newMap) =>
          val completed = newMap.isComplete
          context.become(if (completed) donePlaying(newMap) else playing(newMap))
          out ! Visualizer.printMap(newMap, showMines = completed)
      }
      case _ => out ! unsupportedCommandResponse
    }
  }

  private def newGame(config: NewMap): Unit = {
    Validations.validateMap(config.cols, config.rows, config.mines) match {
      case Some(error) => out ! error
      case _ =>
        val newMap = generateMap(config)
        context.become(playing(newMap))
        out ! Visualizer.printMap(newMap)
    }
  }

  override def receive: Receive = PartialFunction.empty

  override def preStart(): Unit = newGame(Commands.defaultNew)
}
