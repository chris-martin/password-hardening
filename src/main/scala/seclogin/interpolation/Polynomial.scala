package seclogin.interpolation

import collection.immutable.Seq
import Rational.RationalIsFractional._

case class Polynomial(coefficients: Seq[Rational]) extends Function[Rational, Rational] {

  override def apply(x: Rational): Rational =
    coefficients.zipWithIndex.map(
      (
        (a: Rational, i: Int) => a * (x pow i)
      ).tupled
    ).toSeq.sum

  def apply(xs: Seq[Rational]): Seq[Rational] = xs map apply

  override def toString(): String =
    coefficients.zipWithIndex.map({ (a: Rational, i: Int) =>
      "%s%s".format(
        a,
        i match {
          case 0 => ""
          case 1 => "x"
          case _ => "x^%d" format i
        }
      )
    }.tupled).mkString(" + ")

}