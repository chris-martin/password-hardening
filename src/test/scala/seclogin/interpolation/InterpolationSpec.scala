package seclogin.interpolation

import org.scalatest._
import collection.immutable.Seq
import util.Random
import RandomBigInt._

class InterpolationSpec extends FreeSpec {

  "small interpolation" in {
    testInterpolation(
      m = 12,
      q = 173
    )
  }

  "large interpolation" in {
    testInterpolation(
      m = 250,
      q = BigInt("a85d364b1faa7c32a7e0c1676a26e50afb131443", 16)
    )
  }

  def testInterpolation(m: Int, q: BigInt) {

    val r = new Random(123456789)

    val f = Polynomial((1 to m).map(_ => r.nextBigInt(q):Rational))

    val ps: Points = {
      val xs = Seq((1 to m).map(Rational(_)):_*)
      xs zip f(xs)
    }

    val i = ps.truncate(f.coefficients.size).interpolate

    assert(f.coefficients(0).toBigInt == i.toBigInt)

  }

}