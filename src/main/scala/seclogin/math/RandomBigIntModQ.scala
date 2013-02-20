package seclogin.math

import scala.collection.immutable.Set
import scala.collection.mutable
import scala.math.BigInt
import scala.util.Random

/** Adapts a `Random` for use with `BigInt`s in a finite field `Mod` q.
  */
case class RandomBigIntModQ(r: Random = new Random()) {

  /** @return a uniformly random element of ''ℤ,,q,,''
    */
  def nextBigIntModQ()(implicit q: Mod): BigInt = {
    val bits = q.bitLength
    Stream.continually(BigInt(numbits = bits, rnd = r)).filter(_ < q).head
  }

  /** @return a uniformly random size-`n` subset of ''ℤ,,q,,''
    */
  def nextUniqueBigIntsModQ(n: Int)(implicit q: Mod): Set[BigInt] = {
    val set = mutable.Set[BigInt]()
    while (set.size < n) set += nextBigIntModQ()
    set.toSet
  }

}

