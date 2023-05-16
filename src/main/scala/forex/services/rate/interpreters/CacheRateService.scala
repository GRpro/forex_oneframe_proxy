package forex.services.rate.interpreters

import cats.effect.ConcurrentEffect
import cats.implicits._
import forex.domain.{ Currency, Rate }
import forex.services.rate.errors._
import forex.services.rate.errors.Error.{ RateLookupFailed, RateLookupFailedCaused }
import forex.services.rates.{ Algebra => RatesAlgebra }
import forex.services.rate.Algebra
class CacheRateService[F[_]: ConcurrentEffect](rates: RatesAlgebra[F]) extends Algebra[F] {

  override def get(pair: Rate.Pair): F[Error Either Rate] =
    // TODO implement cache
    rates
      .get(Currency.allExchangePairs)
      .map { either =>
        either
          .leftMap(error => RateLookupFailedCaused(error))
          .map(_.rates.find(rate => rate.from == pair.from && rate.to == pair.to))
          .flatMap {
            case Some(rate) =>
              Rate(Rate.Pair(rate.from, rate.to), rate.price, rate.timeStamp).asRight[RateLookupFailed]
            case None =>
              RateLookupFailed(s"Pair not supported $pair").asLeft[Rate]
          }
      }
}
