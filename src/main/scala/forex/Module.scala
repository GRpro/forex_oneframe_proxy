package forex

import cats.effect.{ConcurrentEffect, Timer}
import forex.config.ApplicationConfig
import forex.http.rates.RatesHttpRoutes
import forex.services._
import forex.programs._
import forex.services.rate.interpreters.{BackgroundWorker, ConcurrentRatesCache, RatesCache}
import forex.services.rate.{RateService, RateServices}
import org.http4s._
import org.http4s.implicits._
import org.http4s.server.middleware.{AutoSlash, Timeout}

class Module[F[_]: ConcurrentEffect : Timer](config: ApplicationConfig) {

  private val ratesCache: RatesCache[F] = new ConcurrentRatesCache[F]

  private val ratesService: RatesService[F] = RatesServices.live[F](config.oneFrameClient)

  val worker: BackgroundWorker[F] = new BackgroundWorker[F](ratesService, ratesCache, config.cache.updateInterval)

  private val rateService: RateService[F] = RateServices.live[F](ratesCache, config.cache)

  private val ratesProgram: RatesProgram[F] = RatesProgram[F](rateService)

  private val ratesHttpRoutes: HttpRoutes[F] = new RatesHttpRoutes[F](ratesProgram).routes

  type PartialMiddleware = HttpRoutes[F] => HttpRoutes[F]
  type TotalMiddleware   = HttpApp[F] => HttpApp[F]

  private val routesMiddleware: PartialMiddleware = {
    { http: HttpRoutes[F] =>
      AutoSlash(http)
    }
  }

  private val appMiddleware: TotalMiddleware = { http: HttpApp[F] =>
    Timeout(config.http.timeout)(http)
  }

  private val http: HttpRoutes[F] = ratesHttpRoutes

  val httpApp: HttpApp[F] = appMiddleware(routesMiddleware(http).orNotFound)

}
