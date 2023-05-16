package forex.services

package object rate {
  type RateService[F[_]] = rate.Algebra[F]
  final val RateServices = Interpreters
}
