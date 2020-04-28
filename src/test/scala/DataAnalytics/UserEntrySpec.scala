package DataAnalytics

import DataAnalysis.{Sex, UserEntry}
import org.scalatest.OptionValues
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

class UserEntrySpec extends AnyFlatSpec with Matchers with OptionValues {
  import UserEntrySpec._

  "fromLine" should "parse a basic Male user entry correctly" in {
    val input = correctMale.split(',').toList
    val result = UserEntry.fromLine(input).value

    result.age shouldBe 20
    result.cityId shouldBe 1
    result.sex shouldBe Sex.Male
    result.userId shouldBe 1
  }

  "fromLine" should "parse a basic Female user entry correctly" in {
    val input = correctFemale.split(',').toList
    val result = UserEntry.fromLine(input).value

    result.age shouldBe 30
    result.cityId shouldBe 100
    result.sex shouldBe Sex.Female
    result.userId shouldBe 2
  }

  "fromLine" should "parse a basic Non-binary user entry correctly" in {
    val input = correctNonBinary.split(',').toList
    val result = UserEntry.fromLine(input).value

    result.sex shouldBe Sex.NonBinary
  }

  "fromLine" should "parse a basic Ageless user entry correctly" in {
    val input = correctAgeless.split(',').toList
    val result = UserEntry.fromLine(input).value

    result.age shouldBe -1
  }

  it should "return None when the input is incorrect" in {
    val input1 = "bla,1,1,1".split(',').toList
    val input2 = "1,1,1,bla".split(',').toList

    UserEntry.fromLine(input1) shouldBe None
    UserEntry.fromLine(input2) shouldBe None
  }
}

object UserEntrySpec {
  private val correctMale = "1,1,20,1"
  private val correctFemale = "2,2,30,100"
  private val correctNonBinary = "3,-100,12,1488"
  private val correctAgeless = "4,1,-1,228"
}