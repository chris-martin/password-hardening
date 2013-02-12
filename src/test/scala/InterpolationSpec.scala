package interpolation

import org.scalatest._
import collection.immutable.Seq
import math._

class InterpolationSpec extends FreeSpec {

  "whatever" in {
    val f = Polynomial(Seq(4, 8, 3, 0, 5, 1, 7), MonomialBasis())
    val xs = Seq[Double](1, 2, 3, 7, 11, 12, 17)
    val g = interpolate(xs zip f(xs), MonomialBasis())
    info(g.toString())
    assert(f == g)
  }

}