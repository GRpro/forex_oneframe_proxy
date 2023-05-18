package forex.services.rate.interpreters

import cats.effect.{ Concurrent, Sync }
import forex.domain.Rate

import java.time.OffsetDateTime
import java.util.concurrent.locks.ReentrantReadWriteLock

sealed trait RatesCache[F[_]] {
  def get(key: Rate.Pair): F[(Option[Rate], OffsetDateTime)]
  def updateAll(update: Map[Rate.Pair, Rate], updateTime: OffsetDateTime): F[Unit]
}

class ConcurrentRatesCache[F[_]: Concurrent](lock: ReentrantReadWriteLock = new ReentrantReadWriteLock())
    extends RatesCache[F] {

  private var cache: Map[Rate.Pair, Rate]    = Map.empty
  private var lastUpdateTime: OffsetDateTime = OffsetDateTime.MIN

  override def get(key: Rate.Pair): F[(Option[Rate], OffsetDateTime)] =
    Sync[F].delay {
      val readLock = lock.readLock()
      readLock.lock()
      try {
        cache.get(key) -> lastUpdateTime
      } finally {
        readLock.unlock()
      }
    }

  override def updateAll(update: Map[Rate.Pair, Rate], updateTime: OffsetDateTime): F[Unit] =
    Sync[F].delay {
      val writeLock = lock.writeLock()
      writeLock.lock()
      try {
        cache = update
        lastUpdateTime = updateTime
      } finally {
        writeLock.unlock()
      }
    }
}
