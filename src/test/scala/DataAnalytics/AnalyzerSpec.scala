package DataAnalytics

import DataAnalysis.{Analyzer, HistoryEntry}
import org.scalatest.{BeforeAndAfterAll, OptionValues}
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import akka.stream.scaladsl._

import scala.concurrent.{Await, ExecutionContext}
import scala.util.{Failure, Success}
import scala.concurrent.duration._
import akka.actor.ActorSystem
import akka.testkit.{ImplicitSender, TestActors, TestKit}
import org.scalatest.wordspec.AnyWordSpecLike

class AnalyzerSpec extends TestKit(ActorSystem("MySpec"))
  with Matchers
  with ImplicitSender
  with AnyWordSpecLike
  with BeforeAndAfterAll
{

  override def afterAll: Unit = {
    TestKit.shutdownActorSystem(system)
  }

  "SplitByUserId" must {
    "work correctly on correct input" in {
      val source = Source[HistoryEntry](Seq(HistoryEntry(1, 1.0, 1, 1),
                                            HistoryEntry(1, 1.0, 1, 2),
                                            HistoryEntry(1, 1.0, 1, 1)))

      val userIdFrequency = Analyzer.getUserFrequency(source, 30000).runWith(Sink.seq)
      val result = Await.result(userIdFrequency, 3.seconds).sortWith((first, second) => first._1 < second._1)

      result.length shouldBe 2
      result.head shouldBe (1, 2)
      result(1) shouldBe (2, 1)
    }
  }
}
