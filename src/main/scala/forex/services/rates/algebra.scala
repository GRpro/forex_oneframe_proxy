package forex.services.rates

import forex.domain.Rate
import errors._
import forex.services.rates.interpreters.Protocol.OneFrameApiResponse

trait Algebra[F[_]] {
  def get(pair: List[Rate.Pair]): F[Error Either OneFrameApiResponse]
}
