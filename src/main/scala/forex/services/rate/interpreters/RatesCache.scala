package forex.services.rate.interpreters

import cats.effect.concurrent.Ref
import cats.effect.Concurrent
import cats.implicits._
import forex.domain.Rate

sealed trait RatesCache[F[_]] {
  def get(key: Rate.Pair): F[Option[Rate]]
  def updateAll(update: Map[Rate.Pair, Rate]): F[Unit]
}

object RatesCache {
  def create[F[_]: Concurrent](): RatesCache[F] = new ConcurrentRatesCache[F]()
}

class ConcurrentRatesCache[F[_]: Concurrent]() extends RatesCache[F] {

  private[this] val cache: Ref[F, Map[Rate.Pair, Rate]] = Ref.unsafe(Map.empty)

  override def get(key: Rate.Pair): F[Option[Rate]] =
    cache.get.map(_.get(key))

  override def updateAll(update: Map[Rate.Pair, Rate]): F[Unit] =
    cache.set(update)
}
