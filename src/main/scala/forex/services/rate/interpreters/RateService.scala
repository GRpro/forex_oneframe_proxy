package forex.services.rate.interpreters

import cats.effect.ConcurrentEffect
import cats.implicits._
import forex.domain.Rate
import forex.services.rate.errors._
import forex.services.rate.errors.Error.{ PairNotFound, StaleRate }
import forex.services.rate.Algebra

import java.time.OffsetDateTime
import java.time.temporal.ChronoUnit
import scala.concurrent.duration.FiniteDuration
class RateService[F[_]: ConcurrentEffect](cache: RatesCache[F], staleTimeout: FiniteDuration) extends Algebra[F] {

  override def get(pair: Rate.Pair): F[Error Either Rate] =
    // TODO use time from rate to compare timing
    cache.get(pair).map { result =>
      val now = OffsetDateTime.now()
      result match {
        case (rateOpt, lastUpdate) if now.minus(staleTimeout.toMillis, ChronoUnit.MILLIS).isBefore(lastUpdate) =>
          rateOpt match {
            case Some(rate) =>
              rate.asRight[Error]
            case None =>
              PairNotFound(pair).asLeft[Rate]
          }
        case (_, lastUpdate) =>
          StaleRate(lastUpdate).asLeft[Rate]
      }
    }
}
