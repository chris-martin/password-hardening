package seclogin.math

import scala.util.Random
import scala.collection.immutable.Seq

/** Adapts a `Random` for use with `Polynomial`s.
  */
case class RandomPolynomial(r: Random = new Random()) {

  /** @return a random polynomial with `n` coefficients
    * @param n the number of coefficients in the polynomial
    */
  def nextPolynomial(n: Int)(implicit q: Mod): Polynomial = {
    Polynomial(Stream.fill(n) { r.nextBigIntModQ() })
  }

  /** @return a random sampling of `nrOfPoints` distinct points from `polynomial`
    * @param polynomial a polynomial that will be evaluated
    * @param nrOfPoints the number of evaluations and the size of the returned collection
    */
  def nextPoints(polynomial: Polynomial, nrOfPoints: Int)(implicit q: Mod): Seq[Point] =
    polynomial.points(Seq(r.nextUniqueBigIntsModQ(nrOfPoints).toSeq:_*))

}
