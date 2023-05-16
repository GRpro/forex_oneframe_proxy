package forex.services.rate

import cats.effect.ConcurrentEffect
import forex.services.rate.interpreters._
import forex.services.rates.{Algebra => RatesAlgebra}
object Interpreters {
  def live[F[_]: ConcurrentEffect](ratesAlgebra: RatesAlgebra[F]): Algebra[F] =
    new CacheRateService[F](ratesAlgebra)
}
