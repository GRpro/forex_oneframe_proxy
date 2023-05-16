package forex.services.rates.interpreters
import forex.domain.Rate.Pair
import forex.domain._
import io.circe.generic.extras.Configuration
import io.circe.generic.extras.semiauto.deriveConfiguredDecoder
import io.circe.{ Decoder, DecodingFailure, HCursor }
import forex.http._
object Protocol {

  implicit val configuration: Configuration = Configuration.default.withSnakeCaseMemberNames

  case class OneFrameRate(
      from: Currency,
      to: Currency,
      price: Price,
      timeStamp: Timestamp
  )
  case class OneFrameApiResponse(rates: List[OneFrameRate])

  implicit val currencyDecoder: Decoder[Currency] =
    Decoder.instance[Currency] { c: HCursor =>
      for {
        value <- c.as[String]
        currency <- Currency.fromString(value).toRight(DecodingFailure(s"Unknown currency $value", Nil))
      } yield currency
    }

  implicit val pairDecoder: Decoder[Pair] =
    deriveConfiguredDecoder[Pair]

  implicit val rateDecoder: Decoder[Rate] =
    deriveConfiguredDecoder[Rate]

  implicit val responseDecoder: Decoder[OneFrameRate] =
    deriveConfiguredDecoder[OneFrameRate]

  implicit val oneFrameResponseDecoder: Decoder[OneFrameApiResponse] =
    Decoder.decodeList[OneFrameRate].map(OneFrameApiResponse.apply)
}
