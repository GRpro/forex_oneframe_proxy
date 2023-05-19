package forex.services.rate.interpreters

import cats.effect.{ConcurrentEffect, Fiber}
import cats.implicits._
import forex.domain.Rate
import forex.services.rate.errors._
import forex.services.rate.errors.Error.{PairNotFound, StaleRate}
import forex.services.rate.Algebra

import java.time.OffsetDateTime
import java.time.temporal.ChronoUnit
import scala.concurrent.duration.FiniteDuration

class RateService[F[_]: ConcurrentEffect](cache: RatesCache[F],
                                          backgroundWorker: BackgroundWorker[F],
                                          staleTimeout: FiniteDuration) extends Algebra[F] {

  override def get(pair: Rate.Pair): F[Error Either Rate] =
    cache.get(pair).map { result =>
      val now = OffsetDateTime.now()
      result match {
            case Some(rate) if now.minus(staleTimeout.toMillis, ChronoUnit.MILLIS).isBefore(rate.timestamp.value) =>
              rate.asRight[Error]
            case Some(_) =>
              StaleRate().asLeft[Rate]
            case None =>
              PairNotFound(pair).asLeft[Rate]
          }
    }

  override def start: F[Fiber[F, Unit]] = backgroundWorker.start
}
