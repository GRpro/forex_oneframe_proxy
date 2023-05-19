package forex.services.rate.interpreters

import cats.effect.{ Concurrent, Sync }
import forex.domain.Rate

import java.util.concurrent.locks.ReentrantReadWriteLock

sealed trait RatesCache[F[_]] {
  def get(key: Rate.Pair): F[Option[Rate]]
  def updateAll(update: Map[Rate.Pair, Rate]): F[Unit]
}

object RatesCache {
  def create[F[_]: Concurrent](): RatesCache[F] = new ConcurrentRatesCache[F]()
}

class ConcurrentRatesCache[F[_]: Concurrent](lock: ReentrantReadWriteLock = new ReentrantReadWriteLock())
    extends RatesCache[F] {

  private[this] var cache: Map[Rate.Pair, Rate] = Map.empty

  override def get(key: Rate.Pair): F[Option[Rate]] =
    Sync[F].delay {
      val readLock = lock.readLock()
      readLock.lock()
      try {
        cache.get(key)
      } finally {
        readLock.unlock()
      }
    }

  override def updateAll(update: Map[Rate.Pair, Rate]): F[Unit] =
    Sync[F].delay {
      val writeLock = lock.writeLock()
      writeLock.lock()
      try {
        cache = update
      } finally {
        writeLock.unlock()
      }
    }
}
