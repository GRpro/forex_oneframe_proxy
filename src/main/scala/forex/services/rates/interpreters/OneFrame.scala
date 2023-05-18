package forex.services.rates.interpreters

import forex.http._
import forex.services.rates.Algebra
import cats.effect.ConcurrentEffect
import forex.config.OneFrameClientConfig
import forex.domain.Rate
import forex.services.rates.errors._
import forex.services.rates.interpreters.Protocol.OneFrameApiResponse
import org.http4s.client.Client
import org.http4s.client.blaze.BlazeClientBuilder
import cats.effect._
import cats.implicits._
import com.typesafe.scalalogging.LazyLogging
import forex.services.rates.errors.Error.OneFrameLookupFailed
import org.http4s._
import org.http4s.Method.GET

import scala.concurrent.ExecutionContext.Implicits.global
class OneFrame[F[_]: ConcurrentEffect](oneFrameClientConfig: OneFrameClientConfig)
    extends Algebra[F] with LazyLogging {

  private def queryParameters(pairs: List[Rate.Pair]): String =
    pairs
      .map { pair =>
        s"pair=${pair.from}${pair.to}"
      }
      .mkString("&")
  private def callEffect(client: Client[F], pairs: List[Rate.Pair]): F[Error Either OneFrameApiResponse] = {
    client
      .expect[OneFrameApiResponse](
        Request[F](
          method = GET,
          uri = Uri.unsafeFromString(f"http://${oneFrameClientConfig.host}:${oneFrameClientConfig.port.toString}/rates?${queryParameters(pairs)}"),
//          uri"http://${oneFrameClientConfig.host}:${oneFrameClientConfig.port.toString}/rates?${queryParameters(pairs)}"
          headers = Headers.of(Header("token", oneFrameClientConfig.token))
        )
      )
      // TODO make more succinct and beautiful error handling
      .recover {
        case e: InvalidMessageBodyFailure =>
          logger.error(f"Failed to parse OneFrame response ${e.cause}", e)
          throw e
      }
      .map(_.asRight[Error])
      .handleErrorWith { error =>
        Sync[F].delay(Left(OneFrameLookupFailed(error.getMessage)))
      }
  }

  override def get(pairs: List[Rate.Pair]): F[Error Either OneFrameApiResponse] =
    BlazeClientBuilder[F](global).resource
      .use { client =>
        callEffect(client, pairs)
      }
}
