package seclogin

import scala.util.Random
import scala.collection.immutable.Seq

package object math {

  implicit def enrichRandomForPolynomial(x: Random): RandomPolynomial =
    RandomPolynomial(x)

  implicit def enrichRandomForBigIntsModQ(x: Random): RandomBigIntModQ =
    RandomBigIntModQ(x)

  implicit def enrichRandomForPrimes(x: Random): RandomPrime =
    RandomPrime(x)

  implicit def enrichPointIterableForInterpolation(x: Iterable[Point])
    (implicit q: Mod): Interpolation = Interpolation(Seq(x.toSeq:_*))

}