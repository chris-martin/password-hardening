import collection.immutable.Seq
import math._

package object interpolation {

  def interpolate[B <: Basis](ps: Points, basis: B)
      (implicit bc: BasisConversion[NewtonBasis, B]): Polynomial[B] =
    bc.convert(interpolateNewton(ps), basis)

  def interpolateMonomial(ps: Points): Polynomial[MonomialBasis] =
    interpolate(ps, MonomialBasis())

  def interpolateNewton(ps: Points): Polynomial[NewtonBasis] = Polynomial(
    coefficients = (0 until ps.n).map(ps.dividedDifference(0, _)),
    basis = NewtonBasis(ps.map(_.x))
  )

  implicit val MonomialToMonomial = new BasisConversion[MonomialBasis, MonomialBasis] {

    def convert(f: Polynomial[MonomialBasis], b: MonomialBasis): Polynomial[MonomialBasis] = f

  }

  implicit val NewtonToMonomial = new BasisConversion[NewtonBasis, MonomialBasis] {

    def convert(f: Polynomial[NewtonBasis], b: MonomialBasis): Polynomial[MonomialBasis] =
      (0 until f.coefficients.size).map({ i =>
        f.coefficients(i) *
          (0 until i).map(xi => Polynomial(Seq(-xi, 1), b)).
            fold(Polynomial.constant(1)) { (a, b) => a * b }
      }).fold(Polynomial.constant(0)) { (a, b) => a + b }

  }

  implicit class PolynomialOps[B <: Basis](f: Polynomial[B]) {

    def *(c: Double): Polynomial[B] = Polynomial(f.coefficients.map(_ * c), f.basis)

    def +[C <: Basis](g: Polynomial[C])(implicit bc: BasisConversion[C, B]): Polynomial[B] = {
      val fc: Seq[Double] = f.coefficients
      val gc: Seq[Double] = bc.convert(g, f.basis).coefficients
      val `fc+gc`: Seq[Double] = fc.zipAll(gc, 0d, 0d) map (a => a._1 + a._2)
      Polynomial(`fc+gc`, f.basis)
    }

  }

  implicit class DoubleOps(i: Double) {

    def *[B <: Basis](f: Polynomial[B]): Polynomial[B] = f * i

  }

  implicit class MonomialOps(f: Polynomial[MonomialBasis]) {

    def *(g: Polynomial[MonomialBasis]): Polynomial[MonomialBasis] =
      ???

  }

}

package interpolation {

  case class Point(x: Double, y: Double) {
    def toTuple: (Double, Double) = (x, y)
  }
  object Point {
    implicit def tuple2point(t: (Double, Double)): Point = Point(t._1, t._2)
    implicit def point2tuple(p: Point): (Double, Double) = p.toTuple
  }

  case class Points(ps: Seq[Point]) {

    def toSeq: Seq[Point] = ps
    def n: Int = ps.length
    def x(i: Int): Double = ps(i).x
    def y(i: Int): Double = ps(i).y

    /** [ y_v , ... , y_{v+j} ]
      */
    def dividedDifference(v: Int, `v+j`: Int): Double = {
      val j = `v+j` - v
      assert (0 to n-1-j contains v)
      if (j == 0) return y(v)
      assert (1 to n-1 contains j)
      (dividedDifference(v+1, `v+j`) - dividedDifference(v, `v+j`-1)) / (x(`v+j`) - x(v))
    }

  }
  object Points {

    implicit def pointSeq2points(x: Seq[Point]): Points = new Points(x)

    implicit def tupleSeq2points(x: Seq[(Double, Double)]): Points =
      new Points(x.map(p => p:Point).toSeq)

    implicit def points2seq(x: Points): Seq[Point] = x.toSeq

  }

  trait Basis

  case class MonomialBasis() extends Basis

  case class NewtonBasis(xs: Seq[Double]) extends Basis

  trait BasisConversion[A <: Basis, B <: Basis] {

    def convert(f: Polynomial[A], basis: B): Polynomial[B]

  }

  case class Polynomial[Basis](coefficients: Seq[Double], basis: Basis)
      extends Function[Double, Double] {

    override def apply(x: Double): Double =
      coefficients.zipWithIndex.map({ case (a, i) => a * pow(x, i) }).toSeq.sum

    def apply(xs: Seq[Double]): Seq[Double] = xs map apply

    override def hashCode(): Int = (coefficients, basis).hashCode()

    override def equals(obj: Any): Boolean = obj match {
      case that: Polynomial[_] =>
        this.coefficients == that.coefficients && this.basis == that.basis
      case _ => false
    }

  }

  object Polynomial {

    def constant(c: Double): Polynomial[MonomialBasis] = Polynomial(Seq(c), MonomialBasis())

  }

}