package forex.services.rates

import cats.effect.ConcurrentEffect
import forex.config.OneFrameClientConfig
import interpreters._

object Interpreters {
  def live[F[_]: ConcurrentEffect](oneFrameClientConfig: OneFrameClientConfig): Algebra[F] =
    new OneFrame[F](oneFrameClientConfig)
}
