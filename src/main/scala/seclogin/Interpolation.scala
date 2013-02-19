package seclogin

import scala.collection.immutable.{Seq, Set}
import scala.collection.mutable
import scala.math.BigInt
import scala.util.Random

package object interpolation {

  implicit def enrichRandomForPolynomial(x: Random): RandomPolynomial =
    RandomPolynomial(x)

  implicit def enrichRandomForBigIntsModQ(x: Random): RandomBigIntModQ =
    RandomBigIntModQ(x)

  implicit def enrichPointIterableForInterpolation(x: Iterable[Point])
    (implicit q: Mod): Interpolation = Interpolation(Seq(x.toSeq:_*))

}

package interpolation {

  case class Interpolation(points: Seq[Point])(implicit q: Mod) extends Points {

    def yIntercept: BigInt =
      ((0 until n).map(i => (y(i) * λ(i)) mod q).sum) mod q

    private def λ(i: Int): BigInt =
      (for (j <- 0 until n; if i != j) yield q.divide(x(j), x(j) - x(i))).product mod q

  }

  case class Point(x: BigInt, y: BigInt)

  trait Points {
    def points: Seq[Point]
    def n = points.size
    def x(i: Int): BigInt = points(i).x
    def y(i: Int): BigInt = points(i).y
  }

  case class Polynomial(coefficients: Seq[BigInt])(implicit q: Mod) {

    def a = coefficients
    def n = a.size

    def apply(x: BigInt): BigInt =
      (0 until n).map( i => (a(i) * q.pow(x, i)) mod q ).sum mod q

    def apply(xs: Seq[BigInt]): Seq[BigInt] =
      xs.map(apply(_))

    def points(xs: Seq[BigInt]): Seq[Point] =
      for (x <- xs) yield Point(x, apply(x))

  }

  case class Mod(q: BigInt) {
    def divide(a: BigInt, b: BigInt): BigInt = a * (b modInverse q) mod q
    def pow(a: BigInt, exp: BigInt): BigInt = a modPow (exp, q)
  }

  object Mod {
    implicit def int2Mod(x: Int): Mod = Mod(x)
    implicit def long2Mod(x: Long): Mod = Mod(x)
    implicit def bigInt2Mod(x: BigInt): Mod = Mod(x)
    implicit def mod2BigInt(x: Mod): BigInt = x.q
  }

  case class RandomPolynomial(r: Random = new Random()) {

    def nextPolynomial(n: Int)(implicit q: Mod): Polynomial = {
      Polynomial(Stream.fill(n) { r.nextBigIntModQ() })
    }

    def nextPoints(polynomial: Polynomial, nrOfPoints: Int)(implicit q: Mod): Seq[Point] =
      polynomial.points(Seq(r.nextUniqueBigIntsModQ(nrOfPoints).toSeq:_*))

  }

  case class RandomBigIntModQ(r: Random = new Random()) {

    def nextBigIntModQ()(implicit q: Mod): BigInt = {
      val bits = q.bitLength
      Stream.continually(BigInt(numbits = bits, rnd = r)).filter(_ < q).head
    }

    def nextUniqueBigIntsModQ(n: Int)(implicit q: Mod): Set[BigInt] = {
      val set = mutable.Set[BigInt]()
      while (set.size < n) set += nextBigIntModQ()
      set.toSet
    }

  }

}