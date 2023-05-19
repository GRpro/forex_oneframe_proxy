package forex.domain

import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpecLike

class CurrencySpec extends AnyWordSpecLike with Matchers {

  "Currency" should {
    "be the same after serialization & deserialization" in {
      Currency.allCurrencies.forall(c => Currency.fromString(Currency.show.show(c)).contains(c))
    }
  }
}
