package seclogin

import Rational.RationalIsFractional._

class Interpolation(ps: Points) {

  import ps._

  def interpolate: Rational = {

    (
      (0 until n) map { i =>
        (y(i) * lagrangeCoefficient(i))
      }
    ).sum
  }

  def lagrangeCoefficient(i: Int): Rational = {

    (0 until n).filter(_ != i).map({ j =>
      x(j) / (x(j) - x(i))
    }).product

  }

}
