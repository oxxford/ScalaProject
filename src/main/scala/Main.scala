import java.nio.file.Paths

import DataAnalysis.{Analyzer, HistoryEntry, UserEntry}
import akka.stream.alpakka.csv.scaladsl._
import akka.stream.scaladsl._

import scala.concurrent.ExecutionContext
import scala.util.{Failure, Success}
import ExecutionContext.Implicits.global

object Main {
  def main(args: Array[String]): Unit = {
    val userSource: Source[UserEntry, Any] = FileIO
      .fromPath(Paths.get("users.tsv"))
      .via(CsvParsing.lineScanner(delimiter = '\t'))
      .map(i => UserEntry.fromLine(i.map(_.utf8String)))
      .collect { case Some(v) => v }

    //пока не пригодилось
    //val userMapFuture = userSource.runFold[immutable.HashMap[Int, UserEntry]](new immutable.HashMap[Int, UserEntry]())((map, entry) => map + (entry.userId -> entry))
    val amountOfUsers = Analyzer.getAmountOfUsers(userSource)

    amountOfUsers onComplete {
      case Success(value) => println("Number of users is " + value)
      case Failure(exception) => println(exception)
    }

    val historySource: Source[HistoryEntry, Any] = FileIO
      .fromPath(Paths.get("history.tsv"))
      .via(CsvParsing.lineScanner(delimiter = '\t'))
      .map(i => HistoryEntry.fromLine(i.map(_.utf8String)))
      .collect { case Some(v) => v }

    // почему в этом случае принтит not completed future?
//    val histFuture = amountOfUsers map {
//      numberOfUsers => Analyzer.getStringHistFuture(historySource, numberOfUsers, 20)
//    }

    val histFuture = Analyzer.getStringHistFuture(historySource, 30000, 20)

    histFuture onComplete {
      case Success(value) => println(value)
      case Failure(exception) => println(exception)
    }

    //в будущем думаю сделать еще чет из EDA + хотел подключить spark, но пока не завелось(
  }
}
