package seclogin.interpolation

import org.scalatest._
import scala.util.Random

class InterpolationSpec extends FreeSpec {

  "small interpolation" in {
    implicit val q: Mod = 89
    testInterpolation(6)
  }

  "large interpolation" in {
    implicit val q: Mod = BigInt("a85d364b1faa7c32a7e0c1676a26e50afb131443", 16)
    testInterpolation(250)
  }

  def testInterpolation(m: Int)(implicit q: Mod) {
    val random = new Random(123456789)
    val polynomial = random.nextPolynomial(m)
    val points = random.nextPoints(polynomial, m)
    info("expected: " + polynomial.coefficients(0))
    val i = points.yIntercept
    info("actual: " + i)
    assert(polynomial.coefficients(0) == i)
  }

}