package seclogin.math

import scala.collection.immutable.Set
import scala.collection.mutable
import scala.math.BigInt
import scala.util.Random
import java.security.SecureRandom
import java.math.BigInteger

/** Adapts a `Random` for use with `BigInt`s in a finite field `Mod` q.
  */
case class RandomBigIntModQ(r: Random = new SecureRandom) {

  /** A Java-friendly constructor.
    */
  def this(r: java.util.Random) =
    this(Random.javaRandomToRandom(r))

  /** @return a uniformly random element of ''ℤ,,q,,''
    */
  def nextBigIntModQ()(implicit q: Mod): BigInt = {
    bigIntModQStream().head
  }

  /** @return a uniformly random element of ''ℤ,,q,,'' not equal to the given value
    */
  def nextBigIntModQNotEqualTo(forbidden: BigInt)(implicit q: Mod): BigInt = {
    bigIntModQStream().filter(_ != forbidden).head
  }

  /** @return a uniformly random element of ''ℤ,,q,,'' not equal to the given value
    */
  def nextBigIntegerModQNotEqualTo(forbidden: BigInteger, q: BigInteger): BigInteger = {
    nextBigIntModQNotEqualTo(forbidden)(Mod(q)).bigInteger
  }

  private def bigIntModQStream()(implicit q: Mod): Stream[BigInt] = {
    val bits = q.bitLength
    Stream.continually(BigInt(numbits = bits, rnd = r)).filter(_ < q)
  }

  /** @return a uniformly random size-`n` subset of ''ℤ,,q,,''
    */
  def nextUniqueBigIntsModQ(n: Int)(implicit q: Mod): Set[BigInt] = {
    val set = mutable.Set[BigInt]()
    while (set.size < n) set += nextBigIntModQ()
    set.toSet
  }

}

