package forex.config

import scala.concurrent.duration.FiniteDuration

case class ApplicationConfig(
    http: HttpConfig,
    oneFrameClient: OneFrameClientConfig,
    cache: CacheConfig
)

case class HttpConfig(
    host: String,
    port: Int,
    timeout: FiniteDuration
)

case class OneFrameClientConfig(
    host: String,
    port: Int,
    token: String
)

case class CacheConfig(
    updateInterval: FiniteDuration,
    staleTimeout: FiniteDuration
)
