import java.nio.file.Paths

import DataAnalysis.{Analyzer, HistoryEntry, UserEntry}
import akka.actor.ActorSystem
import akka.http.impl.engine.server.GracefulTerminatorStage
import akka.stream.ActorMaterializer
import akka.stream.alpakka.csv.scaladsl._
import akka.stream.scaladsl._

import scala.concurrent.ExecutionContext
import scala.util.{Failure, Success}

object Main {
  def main(args: Array[String]): Unit = {
    implicit val actors: ActorSystem = ActorSystem()
    implicit val context: ExecutionContext = actors.dispatcher

    val userSource: Source[UserEntry, Any] = FileIO
      .fromPath(Paths.get("users.tsv"))
      .via(CsvParsing.lineScanner(delimiter = '\t'))
      .map(i => UserEntry.fromLine(i.map(_.utf8String)))
      .collect { case Some(v) => v }

    //пока не пригодилось
    //val userMapFuture = userSource.runFold[immutable.HashMap[Int, UserEntry]](new immutable.HashMap[Int, UserEntry]())((map, entry) => map + (entry.userId -> entry))
    val amountOfUsers = Analyzer.getSize(userSource)

    amountOfUsers onComplete {
      case Success(value) =>
        println("Number of users is " + value)
      case Failure(exception) =>
        println(exception)
        actors.terminate()
    }

    val historySource: Source[HistoryEntry, Any] = FileIO
      .fromPath(Paths.get("history.tsv"))
      .via(CsvParsing.lineScanner(delimiter = '\t'))
      .map(i => HistoryEntry.fromLine(i.map(_.utf8String)))
      .collect { case Some(v) => v }

    val userIdFrequency = amountOfUsers map (Analyzer.getUserFrequency(historySource, _))

    val n = userIdFrequency flatMap (Analyzer.getSize(_))
    n onComplete {
      case Success(value) =>
        println("Number of valid users is " + value)
      case Failure(exception) =>
        println(exception)
        actors.terminate()
    }

    val histStringFuture = userIdFrequency flatMap (Analyzer.getStringHistFuture(_, 20))

    histStringFuture onComplete {
      case Success(value) =>
        println(value)
        actors.terminate()
      case Failure(exception) =>
        println(exception)
        actors.terminate()
    }
  }
}
