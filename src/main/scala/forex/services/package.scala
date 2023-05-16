package forex

package object services {
  type RatesService[F[_]] = rates.Algebra[F]
  final val RatesServices = rates.Interpreters

  type RateService[F[_]] = rate.Algebra[F]
  final val RateServices = rate.Interpreters
}
