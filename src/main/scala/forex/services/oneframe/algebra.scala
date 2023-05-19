package forex.services.oneframe

import forex.domain.Rate
import errors._
import forex.services.oneframe.interpreters.Protocol.OneFrameApiResponse

trait Algebra[F[_]] {
  def get(pair: List[Rate.Pair]): F[Error Either OneFrameApiResponse]
}
