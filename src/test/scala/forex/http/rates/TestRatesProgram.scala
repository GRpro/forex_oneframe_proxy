package forex.http.rates

import cats.effect.IO
import forex.domain.{Currency, Price, Rate, Timestamp}
import forex.programs.RatesProgram
import forex.programs.rates.Protocol.GetRatesRequest
import forex.programs.rates.errors._

import java.time.{Instant, ZoneOffset}

class TestRatesProgram extends RatesProgram[IO] {
  val mockResponseMap: Map[GetRatesRequest, Rate] = Map(
    GetRatesRequest(Currency.SGD, Currency.GBP) -> Rate(
      Rate.Pair(Currency.SGD, Currency.GBP),
      Price(BigDecimal(0.87D)),
      Timestamp(Instant.parse("2021-04-25T06:30:30Z").atOffset(ZoneOffset.UTC))
    )
  )
  override def get(request: GetRatesRequest): IO[Either[Error, Rate]] =
    IO.delay(
      Either.cond(
        mockResponseMap.contains(request),
        mockResponseMap(request),
        UserError("Rate not found. Try again later")
      )
    )
}