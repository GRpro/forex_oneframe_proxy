package forex.services.rate.interpreters

import org.scalatest.matchers.should.Matchers
import cats.effect._
import cats.effect.testing.scalatest.AsyncIOSpec
import cats.implicits.catsSyntaxEitherId
import forex.config.CacheConfig
import forex.domain.Currency.{ GBP, JPY, USD }
import forex.domain.{ Price, Rate, Timestamp }
import forex.services.oneframe.interpreters.Protocol.OneFrameApiResponse
import forex.services.oneframe.Algebra
import forex.services.oneframe.errors._
import forex.services.oneframe.errors.Error.OneFrameLookupFailed
import forex.services.rate
import forex.services.oneframe.interpreters.Protocol.OneFrameRate
import org.scalatest.freespec.AsyncFreeSpec
import org.scalatestplus.easymock.EasyMockSugar
import org.easymock.EasyMock._

import java.time.OffsetDateTime
import scala.concurrent.duration._
class RateServiceSpec extends AsyncFreeSpec with AsyncIOSpec with EasyMockSugar with Matchers {

  private val ratesService = mock[Algebra[IO]]

  private val successfulRate = Rate(
    Rate.Pair(USD, GBP),
    Price(Integer.valueOf(10)),
    Timestamp(OffsetDateTime.now())
  )

  private val rateService =
    rate.Interpreters.live(ratesService, CacheConfig(updateInterval = 2.seconds, staleTimeout = 3.second))

  "RateService" - {
    "work" in {

      expect(ratesService.get(anyObject[List[Rate.Pair]]))
        .andReturn(
          IO.pure(
            OneFrameApiResponse(
              List(
                OneFrameRate(
                  successfulRate.pair.from,
                  successfulRate.pair.to,
                  successfulRate.price,
                  successfulRate.timestamp
                )
              )
            ).asRight[Error]
          )
        )
        .once()
      expect(ratesService.get(anyObject[List[Rate.Pair]]))
        .andReturn(
          IO.pure(
            OneFrameLookupFailed("Error").asLeft[OneFrameApiResponse]
          )
        )
        .once()
      replay(ratesService)

      for {
        // Check rate before cache is initialized
        res1 <- rateService.get(successfulRate.pair)
        _ = res1 should be(rate.errors.Error.PairNotFound(successfulRate.pair).asLeft)
        // start background cache update
        fiber <- rateService.start
        _ <- IO.sleep(1.second)
        // check successful result

        res2 <- rateService.get(successfulRate.pair)
        _ = res2 should be(successfulRate.asRight)

        // check unknown pair
        res3 <- rateService.get(Rate.Pair(JPY, USD))
        _ = res3 should be(rate.errors.Error.PairNotFound(Rate.Pair(JPY, USD)).asLeft)

        _ <- IO.sleep(2.second)
        // check cache is stale

        res4 <- rateService.get(successfulRate.pair)
        _ = res4 should be(rate.errors.Error.StaleRate().asLeft)
        _ = verify(ratesService)
        _ <- fiber.cancel
      } yield ()
    }
  }

}
