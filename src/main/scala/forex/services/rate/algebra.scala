package forex.services.rate

import cats.effect.Fiber
import forex.domain.Rate
import forex.services.rate.errors._

trait Algebra[F[_]] {
  def get(pair: Rate.Pair): F[Error Either Rate]
  def start: F[Fiber[F, Unit]]
}
