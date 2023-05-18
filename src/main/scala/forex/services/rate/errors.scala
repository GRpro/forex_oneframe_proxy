package forex.services.rate

import forex.domain.Rate

import java.time.OffsetDateTime
object errors {

  sealed trait Error
  object Error {
    final case class PairNotFound(pair: Rate.Pair) extends Error
    final case class StaleRate(lastUpdate: OffsetDateTime) extends Error
  }


}
