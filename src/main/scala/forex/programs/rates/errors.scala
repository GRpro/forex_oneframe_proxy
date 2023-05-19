package forex.programs.rates

import forex.services.rate.errors.{ Error => RateServiceError }
object errors {

  sealed trait Error

  case class UserError(msg: String) extends Error
  case class ApplicationError(msg: String) extends Error

  def toProgramError(error: RateServiceError): Error = error match {
    case RateServiceError.StaleRate()        => ApplicationError("Rate for is stale, please try again later")
    case RateServiceError.PairNotFound(pair) => UserError(s"Pair $pair is not supported")
  }
}
