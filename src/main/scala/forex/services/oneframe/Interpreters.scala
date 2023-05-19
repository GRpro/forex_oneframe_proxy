package forex.services.oneframe

import cats.effect.ConcurrentEffect
import forex.config.OneFrameClientConfig
import interpreters._

object Interpreters {
  def live[F[_]: ConcurrentEffect](oneFrameClientConfig: OneFrameClientConfig): Algebra[F] =
    new OneFrameClient[F](oneFrameClientConfig)
}
