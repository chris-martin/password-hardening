package seclogin.math

import java.math.BigInteger
import org.scalatest._
import scala.collection.JavaConversions._
import scala.util.Random

class InterpolationSpec extends FreeSpec {

  "6 random coefficients mod 89" in {
    implicit val q: Mod = 89
    testInterpolation(6)
  }

  "200 random coefficients mod a 160-bit prime" in {
    implicit val q: Mod = BigInt("a85d364b1faa7c32a7e0c1676a26e50afb131443", 16)
    testInterpolation(200)
  }

  "[(2,2), (4,3)] mod 11 for java" in {
    val i = new Interpolation(
      Seq(
        BigInteger.valueOf(2), BigInteger.valueOf(2),
        BigInteger.valueOf(4), BigInteger.valueOf(3)
      ),
      BigInteger.valueOf(11)
    )
    assert(i.yIntercept.bigInteger == BigInteger.valueOf(1))
  }

  def testInterpolation(m: Int)(implicit q: Mod) {
    val random = new Random(123456789)
    val polynomial = random.nextPolynomial(m)
    val points = random.nextPoints(polynomial, m)
    val i = points.yIntercept
    assert(polynomial.coefficients(0) == i)
  }

}