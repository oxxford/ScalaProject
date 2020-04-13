import java.nio.file.Paths
import java.util.concurrent.TimeUnit

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.{ContentTypes, HttpEntity}
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.stream.alpakka.csv.scaladsl._
import akka.stream.scaladsl._

import scala.collection.{immutable, mutable}
import scala.concurrent.ExecutionContext
import scala.util.{Failure, Success}

object Main {

  import StreamStages._

  implicit val actors: ActorSystem = ActorSystem()
  implicit val executionContext: ExecutionContext = actors.dispatcher

  def main(args: Array[String]): Unit = {
    val userSource: Source[UserEntry, Any] = FileIO
      .fromPath(Paths.get("users.tsv"))
      .via(CsvParsing.lineScanner(delimiter = '\t'))
      .map(i => UserEntry.fromLine(i.map(_.utf8String)))
      .collect { case Some(v) => v }

    //пока не пригодилось
    val userMapFuture = userSource.runFold[immutable.HashMap[Int, UserEntry]](new immutable.HashMap[Int, UserEntry]())((map, entry) => map + (entry.userId -> entry))

    val historySource: Source[HistoryEntry, Any] = FileIO
      .fromPath(Paths.get("history.tsv"))
      .via(CsvParsing.lineScanner(delimiter = '\t'))
      .map(i => HistoryEntry.fromLine(i.map(_.utf8String)))
      .collect { case Some(v) => v }

    val lenSeqFuture = userMapFuture flatMap ((userMap) => {
      historySource
        .groupBy(userMap.size, _.userId)
        .map(_ -> 1)
        .reduce((l, r) => (l._1, l._2 + r._2))
        .mergeSubstreams
        .map(pair => pair._2)
        .runWith(Sink.seq)
    })

    lenSeqFuture onComplete {
      case Success(a) =>
        // можно настроить кол-во бинов
        val num_bins = 20
        val mx = a.max.toDouble
        val mn = a.min.toDouble
        val hist = a
          .map(x => (((x.toDouble - mn) / (mx - mn)) * num_bins).floor.toInt)
          .groupBy(x => x)
          .map(x => x._1 -> x._2.size)
          .toSeq
          .sortBy(x => x._1)
          .map(x => "From " + (x._1.toDouble / 20 * (mx - mn)).round.toString + " => " + x._2.toString)
        println("Text histogram of advertisement amount seen by users:")
        println(hist.mkString("\n"))
    }

    //в будущем думаю сделать еще чет из EDA + хотел подключить spark, но пока не завелось(

    /*
    val route = (path("instant-metrics") & parameter("symbol".as[String].?)) { symbol =>
      get {
        val stream = historySource
          .map(_.toVector)
          .via(formatter)
        complete(HttpEntity(ContentTypes.`text/plain(UTF-8)`, stream))
      }
    }

    for {
      binding <- Http().bindAndHandle(route, "localhost", 8080)
      _ = sys.addShutdownHook {
        for {
          _ <- binding.terminate(Duration(5, TimeUnit.SECONDS))
          _ <- actors.terminate()
        } yield ()
      }
    } yield ()

     */
  }
}
