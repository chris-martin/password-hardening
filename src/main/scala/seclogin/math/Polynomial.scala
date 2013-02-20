package seclogin.math

import scala.collection.immutable.Seq
import scala.math.BigInt

/** A polynomial represented by its `coefficients` in the standard (monomial) basis.
  * @param coefficients the coefficients for ''(1, x, x^2^, x^3^, ...)'' respectively
  */
case class Polynomial(coefficients: Seq[BigInt])(implicit q: Mod) {

  /** @return the number of coefficients (the degree of the polynomial plus 1,
    * assuming nonzero coefficients)
    */
  def n = coefficients.size

  /** @return ''f(x)'', the value of this function ''f'' at `x`.
    */
  def apply(x: BigInt): BigInt =
    (0 until n).map( i ⇒ (coefficients(i) * q.pow(x, i)) mod q ).sum mod q

  /** @return for each ''x'' in `xs`, the point ''(x, f(x))''
    */
  def points(xs: Seq[BigInt]): Seq[Point] =
    for (x ← xs) yield Point(x, apply(x))

}
