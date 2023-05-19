package forex.services.oneframe.interpreters

import forex.http._
import forex.services.oneframe.Algebra
import cats.effect.ConcurrentEffect
import forex.config.OneFrameClientConfig
import forex.domain.Rate
import forex.services.oneframe.errors._
import forex.services.oneframe.interpreters.Protocol.OneFrameApiResponse
import org.http4s.client.Client
import org.http4s.client.blaze.BlazeClientBuilder
import cats.effect._
import cats.implicits._
import com.typesafe.scalalogging.LazyLogging
import forex.services.oneframe.errors.Error.OneFrameLookupFailed
import org.http4s._
import org.http4s.Method.GET

import scala.concurrent.ExecutionContext.Implicits.global

class OneFrameClient[F[_]: ConcurrentEffect](oneFrameClientConfig: OneFrameClientConfig)
    extends Algebra[F]
    with LazyLogging {

  private def queryParameters(pairs: List[Rate.Pair]): String =
    pairs
      .map { pair =>
        s"pair=${pair.from}${pair.to}"
      }
      .mkString("&")

  private def callEffect(client: Client[F], pairs: List[Rate.Pair]): F[Error Either OneFrameApiResponse] =
    client
      .expect[OneFrameApiResponse](
        Request[F](
          method = GET,
          uri = Uri.unsafeFromString(
            f"http://${oneFrameClientConfig.host}:${oneFrameClientConfig.port.toString}/rates?${queryParameters(pairs)}"
          ),
          headers = Headers.of(Header("token", oneFrameClientConfig.token))
        )
      )
      .map(_.asRight[Error])
      .handleErrorWith {
        case error: InvalidMessageBodyFailure =>
          logger.error(f"Failed to parse OneFrame response ${error.cause}", error)
          Sync[F].delay(OneFrameLookupFailed(error.getMessage).asLeft)
        case error: Throwable =>
          logger.error(f"OneFrame client error", error)
          Sync[F].delay(OneFrameLookupFailed(error.getMessage).asLeft)
      }

  override def get(pairs: List[Rate.Pair]): F[Error Either OneFrameApiResponse] =
    BlazeClientBuilder[F](global).resource
      .use { client =>
        callEffect(client, pairs)
      }
}
