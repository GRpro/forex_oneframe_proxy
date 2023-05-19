package forex.services.rate

import cats.effect.{ConcurrentEffect, Timer}
import forex.config.CacheConfig
import forex.services.rate.interpreters._
import forex.services.oneframe.{Algebra => RatesAlgebra}

object Interpreters {
  def live[F[_]: ConcurrentEffect: Timer](ratesAlgebra: RatesAlgebra[F], cacheConfig: CacheConfig): Algebra[F] = {
    val cache = RatesCache.create[F]()
    val backgroundWorker = new BackgroundWorker[F](ratesAlgebra, cache, cacheConfig.updateInterval)
    new RateService[F](cache, backgroundWorker, cacheConfig.staleTimeout)
  }
}
