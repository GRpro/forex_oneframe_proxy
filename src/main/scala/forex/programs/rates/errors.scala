package forex.programs.rates

import forex.services.rate.errors.{ Error => RateServiceError }
object errors {

  final case class Error(msg: String) extends Exception(msg)

  def toProgramError(error: RateServiceError): Error = error match {
    case RateServiceError.StaleRate(_)       => Error("Stale rate")
    case RateServiceError.PairNotFound(pair) => Error(s"Pair $pair not found")
  }
}
