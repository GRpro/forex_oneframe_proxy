package forex.domain

import cats.Show
import cats.implicits.catsSyntaxOptionId
import forex.domain.Rate.Pair

sealed trait Currency

object Currency {
  case object AUD extends Currency
  case object CAD extends Currency
  case object CHF extends Currency
  case object EUR extends Currency
  case object GBP extends Currency
  case object NZD extends Currency
  case object JPY extends Currency
  case object SGD extends Currency
  case object USD extends Currency

  implicit val show: Show[Currency] = Show.show {
    case AUD => "AUD"
    case CAD => "CAD"
    case CHF => "CHF"
    case EUR => "EUR"
    case GBP => "GBP"
    case NZD => "NZD"
    case JPY => "JPY"
    case SGD => "SGD"
    case USD => "USD"
  }

  def fromString(s: String): Option[Currency] = s.toUpperCase match {
    case "AUD" => AUD.some
    case "CAD" => CAD.some
    case "CHF" => CHF.some
    case "EUR" => EUR.some
    case "GBP" => GBP.some
    case "NZD" => NZD.some
    case "JPY" => JPY.some
    case "SGD" => SGD.some
    case "USD" => USD.some
    case _ => None
  }

  lazy val allCurrencies: List[Currency] = List(AUD, CAD, CHF, EUR, GBP, NZD, JPY, SGD, USD)

  lazy val allExchangePairs: List[Pair] = {
    for {
      currency1 <- allCurrencies
      currency2 <- allCurrencies
    } yield Pair(currency1, currency2)
  }
}
