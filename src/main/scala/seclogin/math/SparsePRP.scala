package seclogin.math

import scala.collection.mutable.{ArrayBuffer, HashSet}

/** A pseudo-random permutation (PRP) in ''ℤ,,q,,'' based on a `PRG`.
  * This PRP is considered "sparse" because it is efficient only over small inputs.
  */
class SparsePRP(prg: PRG)(implicit q: Mod) {

  /** Constructor using the default PRG with a given `key`.
    */
  def this(key: Array[Byte])(implicit q: Mod) = this(new PRG(key))

  /** Constructor using the default PRG with a default key.
    */
  def this()(implicit q: Mod) = this(Array[Byte]())

  /** Java-friendly constructor.
    */
  def this(key: Array[Byte], q: java.math.BigInteger) = this(key)(q: BigInt)

  private val seq = new ArrayBuffer[BigInt]
  private val set = new HashSet[BigInt]

  def apply(i: Int): BigInt = {
    while (seq.size <= i) generateAnother()
    seq(i)
  }

  private def generateAnother() {
    val x = Stream.continually(prg.nextBigIntModQ()).filter(t => !(set contains t)).head
    seq += x
    set += x
  }

}
