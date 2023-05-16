package forex.http.rates

import forex.domain.Currency
import org.http4s.{ ParseFailure, QueryParamDecoder }
import org.http4s.dsl.impl.QueryParamDecoderMatcher

object QueryParams {

  private[http] implicit val currencyQueryParam: QueryParamDecoder[Currency] =
    QueryParamDecoder[String].emap(
      value => Currency.fromString(value).toRight(ParseFailure(s"Unknown currency $value", ""))
    )

  object FromQueryParam extends QueryParamDecoderMatcher[Currency]("from")
  object ToQueryParam extends QueryParamDecoderMatcher[Currency]("to")

}
