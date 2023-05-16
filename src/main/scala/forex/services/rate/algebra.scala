package forex.services.rate

import forex.domain.Rate
import forex.services.rate.errors._

trait Algebra[F[_]] {
  def get(pair: Rate.Pair): F[Error Either Rate]
}
