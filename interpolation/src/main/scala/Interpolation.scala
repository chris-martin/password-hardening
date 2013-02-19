package seclogin

import scala.collection.immutable.{Seq, Set}
import scala.collection.mutable
import scala.math.BigInt
import scala.util.Random

package interpolation {

  /** Interpolation for the unique minimal-degree polynomial
    * ''f : ℤ,,q,, → ℤ,,q,,'' containing all of the `points`.
    * @param points ''(x, y)'' pairs that reside in ''f''
    * @param q size of the field over which ''f'' is defined
    */
  case class Interpolation(points: Seq[Point])(implicit q: Mod) extends Points {

    /** @return the value of the interpolated polynomial evaluated at `0`
      */
    def yIntercept: BigInt = {
      val terms = for (i ← 0 until n) yield { (y(i) * λ(i)) mod q }
      terms.sum mod q
    }

    /** @return the `i`^th^ Lagrange coefficient
      */
    def λ(i: Int): BigInt = {
      val terms = for (j ← 0 until n; if i != j) yield { q.divide(x(j), x(j) - x(i)) }
      terms.product mod q
    }

  }

  /** An ''(x, y)'' pair contained by some function ''f(x) = y''.
    */
  case class Point(x: BigInt, y: BigInt)

  trait Points {
    def points: Seq[Point]
    def n = points.size
    def x(i: Int): BigInt = points(i).x
    def y(i: Int): BigInt = points(i).y
  }

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

  /** Represents ''ℤ,,q,,'', the field of integers ''[0, q)''.
    * @param q the size of the field
    */
  case class Mod(q: BigInt) {

    /** @return ''`a`/`b` (mod `q`)''
      */
    def divide(a: BigInt, b: BigInt): BigInt = a * (b modInverse q) mod q

    /** @return ''`base`^`exp`^ (mod `q`)''
      */
    def pow(base: BigInt, exp: BigInt): BigInt = base modPow (exp, q)

  }

  object Mod {
    implicit def int2Mod(x: Int): Mod = Mod(x)
    implicit def long2Mod(x: Long): Mod = Mod(x)
    implicit def bigInt2Mod(x: BigInt): Mod = Mod(x)
    implicit def mod2BigInt(x: Mod): BigInt = x.q
  }

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

  /** Adapts a `Random` for use with `BigInt`s in a finite field `Mod` q.
    */
  case class RandomBigIntModQ(r: Random = new Random()) {

    /** @return a uniformly random element of ''ℤ,,q,,''
      */
    def nextBigIntModQ()(implicit q: Mod): BigInt = {
      val bits = q.bitLength
      Stream.continually(BigInt(numbits = bits, rnd = r)).filter(_ < q).head
    }

    /** @return a uniformly random size-`n` subset of ''ℤ,,q,,''
      */
    def nextUniqueBigIntsModQ(n: Int)(implicit q: Mod): Set[BigInt] = {
      val set = mutable.Set[BigInt]()
      while (set.size < n) set += nextBigIntModQ()
      set.toSet
    }

  }

}

package object interpolation {

  implicit def enrichRandomForPolynomial(x: Random): RandomPolynomial =
    RandomPolynomial(x)

  implicit def enrichRandomForBigIntsModQ(x: Random): RandomBigIntModQ =
    RandomBigIntModQ(x)

  implicit def enrichPointIterableForInterpolation(x: Iterable[Point])
    (implicit q: Mod): Interpolation = Interpolation(Seq(x.toSeq:_*))

}