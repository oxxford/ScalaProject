package DataAnalytics

import DataAnalysis.{HistoryEntry}
import org.scalatest.OptionValues
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

class HistoryEntrySpec extends AnyFlatSpec with Matchers with OptionValues {
  import HistoryEntrySpec._

  "fromLine" should "parse a basic history entry correctly" in {
    val input = correct.split(',').toList
    val result = HistoryEntry.fromLine(input).value

    result.hour shouldBe 12
    result.cpm shouldBe 10.0
    result.publisherId shouldBe 1
    result.userId shouldBe 1
  }

  it should "should return None if input is incorrect" in {
    val input1 = "25,10.0,1,1".split(',').toList
    val input2 = "12,-10.0,1,1".split(',').toList
    val input3 = "12,10.0,bla,1".split(',').toList
    val input4 = "12,10.0,1,bla".split(',').toList

    HistoryEntry.fromLine(input1) shouldBe None
    HistoryEntry.fromLine(input2) shouldBe None
    HistoryEntry.fromLine(input3) shouldBe None
    HistoryEntry.fromLine(input4) shouldBe None
  }

}

object HistoryEntrySpec {
  private val correct = "12,10.0,1,1"
}
