package forex.services.rate

import forex.domain.Rate

object errors {

  sealed trait Error
  object Error {
    final case class PairNotFound(pair: Rate.Pair) extends Error
    final case class StaleRate() extends Error
  }


}
