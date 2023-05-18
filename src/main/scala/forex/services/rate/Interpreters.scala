package forex.services.rate

import cats.effect.ConcurrentEffect
import forex.config.CacheConfig
import forex.services.rate.interpreters._

object Interpreters {
  def live[F[_]: ConcurrentEffect](cache: RatesCache[F], cacheConfig: CacheConfig): Algebra[F] =
    new RateService[F](cache, cacheConfig.staleTimeout)
}
