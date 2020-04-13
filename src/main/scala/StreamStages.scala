import akka.stream.alpakka.csv.scaladsl.CsvFormatting
import akka.stream.scaladsl.{Flow, Source}
import akka.util.ByteString

object StreamStages {
  val formatter: Flow[Vector[String], ByteString, Any] = CsvFormatting.format()
}
