package forex.services.rate

import forex.services.rates.errors.{Error => RatesError}
object errors {

  sealed trait Error
  object Error {
    final case class RateLookupFailed(msg: String) extends Error
    final case class RateLookupFailedCaused(cause: RatesError) extends Error
  }


}
