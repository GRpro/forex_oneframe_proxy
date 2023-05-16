package forex.programs.rates

import cats.Functor
import cats.data.EitherT
import errors._
import forex.domain._
import forex.services.RateService

class Program[F[_]: Functor](
    rateService: RateService[F]
) extends Algebra[F] {

  override def get(request: Protocol.GetRatesRequest): F[Error Either Rate] =
    EitherT(rateService.get(Rate.Pair(request.from, request.to))).leftMap(toProgramError).value

}

object Program {

  def apply[F[_]: Functor](
      rateService: RateService[F]
  ): Algebra[F] = new Program[F](rateService)

}
