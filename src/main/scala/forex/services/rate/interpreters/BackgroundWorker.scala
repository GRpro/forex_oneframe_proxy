package forex.services.rate.interpreters

import cats.effect.{ Concurrent, Fiber, Timer }
import cats.implicits._
import com.typesafe.scalalogging.LazyLogging

import scala.concurrent.duration._
import forex.domain.{ Currency, Rate }
import forex.services.oneframe.{ Algebra => RatesAlgebra }

class BackgroundWorker[F[_]: Concurrent: Timer](rates: RatesAlgebra[F],
                                                cache: RatesCache[F],
                                                updateInterval: FiniteDuration)
    extends LazyLogging {

  def start: F[Fiber[F, Unit]] =
    Concurrent[F].start(loop.foreverM.void)

  private[this] def loop: F[Unit] =
    for {
      // TODO Add retries for "retriable errors" like network issues.
      //      Optionally, think of a way to suspend calls to OneFrame API when no one is using service
      //      for a configured period, and resume the loop when next request comes in case the loop is suspended.
      //      This should be a good trade-off between client request SLA and decreasing idle usage of OneFrame API
      _ <- tryUpdateCache
      _ <- Timer[F].sleep(updateInterval)
    } yield ()

  private[this] def tryUpdateCache: F[Unit] = {
    logger.info("Attempt to update cache")
    // TODO if the list of currencies grow, update in multiple requests
    rates
      .get(Currency.allExchangePairs)
      .flatMap {
        case Right(response) =>
          val rates = response.rates.map { rate =>
            val pair = Rate.Pair(rate.from, rate.to)
            pair -> Rate(pair, rate.price, rate.timeStamp)
          }.toMap

          for {
            _ <- cache.updateAll(rates)
          } yield logger.info(s"Cache updated with ${rates.size} pairs")
        case Left(error) =>
          logger.info(s"Failed to update cache $error")
          Concurrent[F].unit
      }
  }

}
