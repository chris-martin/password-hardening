package interpolation

import org.scalatest._
import collection.immutable.Seq
import math._

class InterpolationSpec extends FreeSpec {

  "short" in {
    val f = Polynomial(Seq(4, 8, 3, 0, 5, 1, 7), MonomialBasis())
    val xs = Seq[Double](1, 2, 3, 7, 11, 12, 17)
    val g = interpolate(xs zip f(xs), MonomialBasis())
    info(f.stringify)
    info(g.stringify)
    assert(f == g)
  }

  "long" in {
    val r = new java.util.Random()
    val f = Polynomial((1 to 11).map(_ => r.nextDouble()), MonomialBasis())
    val xs = (1 to 10).map(_.toDouble).toSeq
    val g = interpolate(xs zip f(xs), MonomialBasis())
    info(f.stringify)
    info(g.stringify)
    assert(f == g)
  }

}