package forex.services.rate.interpreters

import cats.effect.{Concurrent, Fiber, Timer}
import cats.implicits._
import com.typesafe.scalalogging.LazyLogging

import scala.concurrent.duration._
import forex.domain.{Currency, Rate}
import forex.services.rates.{Algebra => RatesAlgebra}

import java.time.OffsetDateTime
class BackgroundWorker[F[_]: Concurrent: Timer](rates: RatesAlgebra[F],
                                                cache: RatesCache[F],
                                                updateInterval: FiniteDuration) extends LazyLogging {

  def runFiber: F[Fiber[F, Unit]] =
    Concurrent[F].start(loop.foreverM.void)

  private def loop: F[Unit] =
    for {
      // TODO maybe add retries
      _ <- tryUpdateCache
      _ <- Timer[F].sleep(updateInterval)
    } yield ()

  private def tryUpdateCache: F[Unit] =
    rates
      .get(Currency.allExchangePairs)
      .flatMap {
          case Right(response) =>
            val rates = response.rates.map { rate =>
              val pair = Rate.Pair(rate.from, rate.to)
              pair -> Rate(pair, rate.price, rate.timeStamp)
            }.toMap
            val updateTime = OffsetDateTime.now()
            cache.updateAll(rates, updateTime).map { _ =>
              logger.info(s"Cache updated with ${rates.size} pairs")
            }

          case Left(error) =>
            logger.info(s"Failed to update cache $error")
            Concurrent[F].unit
      }

}
