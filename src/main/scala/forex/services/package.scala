package forex

package object services {
  type RatesService[F[_]] = oneframe.Algebra[F]
  final val RatesServices = oneframe.Interpreters

  type RateService[F[_]] = rate.Algebra[F]
  final val RateServices = rate.Interpreters
}
