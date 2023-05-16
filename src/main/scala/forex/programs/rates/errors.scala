package forex.programs.rates

import forex.services.rates.errors.{ Error => RatesServiceError }
import forex.services.rate.errors.{ Error => RateServiceError }
object errors {

  final case class Error(msg: String) extends Exception(msg)

  def toProgramError(error: RateServiceError): Error = error match {
    case RateServiceError.RateLookupFailed(msg) => Error(msg)
    case RateServiceError.RateLookupFailedCaused(cause) =>
      cause match {
        case RatesServiceError.OneFrameLookupFailed(_) =>
//          println(msg)
          Error("failed to parse")
      }
  }
}
