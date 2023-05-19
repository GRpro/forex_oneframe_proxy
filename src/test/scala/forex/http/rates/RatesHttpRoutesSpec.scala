package forex.http.rates

import cats.effect.IO
import forex.http.jsonDecoder
import io.circe.{ parser, Json }
import org.http4s.Request
import org.http4s.implicits.http4sLiteralsSyntax
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AsyncWordSpecLike

class RatesHttpRoutesSpec extends AsyncWordSpecLike with Matchers {
  val program = new TestRatesProgram
  val routes  = new RatesHttpRoutes[IO](program)

  "RatesHttpRoutes" should {

    "return error when the actual rate is missing" in {
      val request  = Request[IO](uri = uri"/rates?from=USD&to=AUD")
      val expected = "Rate not found. Try again later"
      val actual   = routes.routes(request).value.flatMap(_.get.as[Json]).unsafeRunSync()
      actual.hcursor.downField("error").as[String].getOrElse("Empty message!") shouldEqual expected
    }

    "doesn't match when the `from` or `to` currency in invalid" in {
      routes.routes(Request[IO](uri = uri"/rates?from=XYZ&to=USD")).value.unsafeRunSync() shouldBe None
      routes.routes(Request[IO](uri = uri"/rates?from=USD&to=XYZ")).value.unsafeRunSync() shouldBe None

    }

    "return exchange rate for valid `from` & `to` currencies" in {
      val request  = Request[IO](uri = uri"/rates?from=SGD&to=GBP")
      val expected = parser.parse("""
                        {
                          "from": "SGD",
                          "to": "GBP",
                          "price": 0.87,
                          "timestamp": "2021-04-25T06:30:30Z"
                        }
                        """).getOrElse(Json.Null)

      routes.routes(request).value.flatMap(_.get.as[Json]).unsafeRunSync() should equal(expected)
    }
  }
}
