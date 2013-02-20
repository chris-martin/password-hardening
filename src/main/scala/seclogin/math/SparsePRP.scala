package seclogin.math

import scala.collection.mutable.{ArrayBuffer, HashSet}

class SparsePRP(prg: PRG)(implicit q: Mod) {

  def this(key: Array[Byte])(implicit q: Mod) = this(new PRG(key))

  /** Java-friendly constructor.
    */
  def this(key: Array[Byte], q: java.math.BigInteger) = this(key)(q: BigInt)

  val seq = new ArrayBuffer[BigInt]
  val set = new HashSet[BigInt]

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
