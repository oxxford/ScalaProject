import Sex.{Female, Male}

import scala.collection.immutable.HashMap
import scala.collection.mutable

sealed trait Sex

object Sex {
  case object Male extends Sex
  case object Female extends Sex
}


final case class UserEntry(userId: Int, sex: Sex, age: Int, cityId: Int) {
  def toVector: Vector[String] = Vector(userId.toString, sex.toString, age.toString, cityId.toString)
}

object UserEntry {
  def fromLine(line: List[String]): Option[UserEntry] = {
    line match {
      case id +: sex +: age +: city =>
        for {
          userId <- id.toIntOption
          userSex <- sex match {
            case "1" => Option(Male)
            case "2" => Option(Female)
            case _ => None
          }
          userAge <- age.toIntOption
          userCity <- city.head.toIntOption
        } yield UserEntry(userId, userSex, userAge, userCity)
      case _ => None
    }
  }
}

final case class HistoryEntry(hour: Int, cpm: Double, publisher: Int, userId: Int) {
  def toVector: Vector[String] = Vector(hour.toString, cpm.toString, publisher.toString, userId.toString)
}

object HistoryEntry {
  def fromLine(line: List[String]): Option[HistoryEntry] = {
    line match {
      case hour +: cpm +: publisher +: userId =>
        for {
          historyHour <- hour.toIntOption
          historyCpm <- cpm.toDoubleOption
          historyPublisher <- publisher.toIntOption
          historyUserId <- userId.head.toIntOption

        } yield HistoryEntry(historyHour, historyCpm, historyPublisher, historyUserId)
      case _ => None
    }
  }
}

