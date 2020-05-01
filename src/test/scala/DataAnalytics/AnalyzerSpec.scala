package DataAnalytics

import DataAnalysis.{Analyzer, HistoryEntry, Sex, UserEntry}
import org.scalatest.{BeforeAndAfterAll, OptionValues}
import org.scalatest.matchers.should.Matchers
import akka.stream.scaladsl._

import scala.concurrent.{Await, ExecutionContext}
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

  "getSize" must {
    "work correctly on simple input" in {
      val intSource = Source[UserEntry](Seq(DataAnalysis.UserEntry(1, Sex.Male, 22, 228),
                                            DataAnalysis.UserEntry(2, Sex.Female, 69, 322),
                                            DataAnalysis.UserEntry(3, Sex.NonBinary, 18, 1488)
                                            ))

      val sizeFuture = Analyzer.getSize(intSource)
      val result = Await.result(sizeFuture, 3.seconds)

      result shouldBe 3
    }

    "work correctly on complex input" in {
      val intSource = Source[(Int, Int)](Seq((1, 2), (2, 1), (3, 4), (4, 9)))

      val sizeFuture = Analyzer.getSize(intSource)
      val result = Await.result(sizeFuture, 3.seconds)

      result shouldBe 4
    }
  }

  "getHist" must {
    "work correctly on input with repeating values" in {
      implicit val ex: ExecutionContext = system.dispatcher

      val source = Source[(Int, Int)](Seq((1, 1), (2, 1), (3, 3), (4, 3), (5, 6), (6, 6)))

      val histFuture = Analyzer.getStringHistFuture(source, 2)
      val result = Await.result(histFuture, 3.seconds)

      result shouldBe "From 1 => 4\nFrom 4 => 2"
    }

    "work correctly on input with unique values" in {
      implicit val ex: ExecutionContext = system.dispatcher

      val source = Source[(Int, Int)](Seq((1, 1), (2, 2), (3, 3), (4, 4), (5, 5), (6, 6)))

      val histFuture = Analyzer.getStringHistFuture(source, 2)
      val result = Await.result(histFuture, 3.seconds)

      result shouldBe "From 1 => 3\nFrom 4 => 3"
    }

    "work correctly with 1 bin" in {
      implicit val ex: ExecutionContext = system.dispatcher

      val source = Source[(Int, Int)](Seq((1, 1), (2, 2), (3, 3), (4, 4), (5, 5), (6, 6)))

      val histFuture = Analyzer.getStringHistFuture(source, 1)
      val result = Await.result(histFuture, 3.seconds)

      result shouldBe "From 1 => 6"
    }

    "work correctly with n bins" in {
      implicit val ex: ExecutionContext = system.dispatcher

      val source = Source[(Int, Int)](Seq((1, 1), (2, 2), (3, 3), (4, 4), (5, 5), (6, 6)))

      val histFuture = Analyzer.getStringHistFuture(source, 6)
      val result = Await.result(histFuture, 3.seconds)

      result shouldBe "From 1 => 1\nFrom 2 => 1\nFrom 3 => 1\nFrom 4 => 1\nFrom 5 => 1\nFrom 6 => 1"
    }

    "work correctly with odd number of bins" in {
      implicit val ex: ExecutionContext = system.dispatcher

      val source = Source[(Int, Int)](Seq((1, 1), (2, 2), (3, 3), (4, 4), (5, 5), (6, 6)))

      val histFuture = Analyzer.getStringHistFuture(source, 3)
      val result = Await.result(histFuture, 3.seconds)

      result shouldBe "From 1 => 2\nFrom 3 => 2\nFrom 5 => 2"
    }

    "work correctly with odd number of bins with repeating input" in {
      implicit val ex: ExecutionContext = system.dispatcher

      val source = Source[(Int, Int)](Seq((1, 1), (2, 1), (3, 3), (4, 3), (5, 6), (6, 6)))

      val histFuture = Analyzer.getStringHistFuture(source, 3)
      val result = Await.result(histFuture, 3.seconds)

      result shouldBe "From 1 => 2\nFrom 3 => 2\nFrom 5 => 2"
    }
  }
}
