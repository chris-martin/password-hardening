package seclogin.interpolation

import util.Random

class RandomBigInt(r: Random) {

  def nextBigInt(limit: BigInt): BigInt = {
    if (limit <= 0) throw new IllegalArgumentException()
    val bits = limit.bitLength
    var x: BigInt = null
    var done = false
    while (!done) {
      x = scala.math.BigInt(numbits = bits, rnd = r)
      if (x < limit) done = true
    }
    x
  }

}

object RandomBigInt {

  implicit def random2randomBigInt(r: Random): RandomBigInt = new RandomBigInt(r)

}
