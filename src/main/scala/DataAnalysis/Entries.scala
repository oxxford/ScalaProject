package DataAnalysis

import DataAnalysis.Sex.{Female, Male, NonBinary}

sealed trait Sex

object Sex {
  case object Male extends Sex
  case object Female extends Sex
  case object NonBinary extends Sex
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
            case _ => Option(NonBinary)
          }
          userAge <- age match {
            case value if value.toInt > 0 => value.toIntOption
            case _ => Option(-1)
          }
          userCity <- city.head.toIntOption
        } yield UserEntry(userId, userSex, userAge, userCity)
      case _ => None
    }
  }
}

final case class HistoryEntry(hour: Int, cpm: Double, publisherId: Int, userId: Int) {
  def toVector: Vector[String] = Vector(hour.toString, cpm.toString, publisherId.toString, userId.toString)
}

object HistoryEntry {
  def fromLine(line: List[String]): Option[HistoryEntry] = {
    line match {
      case hour +: cpm +: publisher +: userId =>
        for {
          historyHour <- hour match {
            case value if (value.toInt <= 23 && value.toInt >= 0) => value.toIntOption
            case _ => None
          }
          historyCpm <- cpm match {
            case value if value.toDouble >= 0 => value.toDoubleOption
            case _ => None
          }
          historyPublisher <- publisher.toIntOption
          historyUserId <- userId.head.toIntOption
        } yield HistoryEntry(historyHour, historyCpm, historyPublisher, historyUserId)
      case _ => None
    }
  }
}

