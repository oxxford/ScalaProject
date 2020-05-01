package DataAnalysis

import akka.actor.ActorSystem
import akka.stream.scaladsl.{Sink, Source}

import scala.concurrent.{ExecutionContext, Future}

object Analyzer{
  def getSize(userSource: Source[_, Any])(implicit actorsSystem: ActorSystem): Future[Int] = {
    userSource.runWith(Sink.fold(0)((sum, _) => sum + 1))
  }

  def getUserFrequency(historySource: Source[HistoryEntry, Any], maxUsers: Int): Source[(Int, Int), Any] = {
    historySource
      .groupBy(maxUsers, _.userId)
      .map(_ -> 1)
      .reduce(
        (l, r) => (l._1, l._2, r._2) match {
          // for readability
          case (historyEntryWithCurrentId: HistoryEntry, inclusionFirst: Int, inclusionSecond: Int) =>
            (historyEntryWithCurrentId, inclusionFirst + inclusionSecond)
        }
      )
      .mergeSubstreams
      .map(pair => (pair._1.userId, pair._2))
  }

  def getStringHistFuture(source: Source[(Int, Int), Any], numBins: Int)
                         (implicit actorsSystem: ActorSystem, context: ExecutionContext): Future[String] = {
    source.map(
        pair => pair._2 match {
          // for readability
          case amountOfHistoryEntriesForUser: Int => amountOfHistoryEntriesForUser
        }
      )
      .runWith(Sink.seq)
      .map { lenSeq =>
        val mx = lenSeq.max.toDouble
        val mn = lenSeq.min.toDouble
        "Text histogram of advertisement amount seen by users:\n" +
          lenSeq
            .map(x => (((x.toDouble - mn) / (mx - mn)) * numBins).floor.toInt)
            .groupBy {
              // for readability
              binNumber: Int => binNumber
            }
            .map(x => (x._1, x._2.size) match {
              // for readability
              case (binNumber: Int, binSize: Int) => binNumber -> binSize
            })
            .toSeq
            .sortBy(x => x._1 match {
              // for readability
              case binNumber: Int => binNumber
            })
            .map(x => "From " + (x._1.toDouble / 20 * (mx - mn)).round.toString + " => " + x._2.toString)
            .mkString("\n")
      }
  }
}
