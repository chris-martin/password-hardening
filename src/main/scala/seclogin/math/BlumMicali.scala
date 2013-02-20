package seclogin.math

import scala.util.Random
import java.security.SecureRandom
import BlumMicali._

/** The Blum–Micali pseudorandom generator algorithm.
  * @param p a safe prime
  * @param s seed value ∈ ''ℤ,,p,,''
  */
class BlumMicali(val p: Mod, private var s: BigInt = 0) extends Generator {

  def this(numbits: Int = 160, r: Random = new SecureRandom) =
    this(r.nextSafePrime(numbits))

  private val `(p-1)/2` = (p-1)/2

  /** Sets the seed for this generator.
    * @param x the new seed value
    */
  def seed(x: BigInt): this.type = {
    s = x mod p
    this
  }

  def seed(r: Random = new SecureRandom): this.type =
    seed(r.nextBigIntModQ()(p))

  def nextBit(): Boolean = {

    // s ← g^s (mod p)
    s = g modPow (s, p)

    // return the most significant bit of the new seed
    s >= `(p-1)/2`
  }

  // todo def nextBigInt(numbits: Int): BigInt = BigInt(Stream.fill(numbits)(nextBit()).toArray)

}

object BlumMicali {

  /** In a prime-order group, all elements excluding the trivial subgroup are generators.
    * In ''ℤ^*^,,p,,'' where ''p'' is a safe prime, this means that any odd number greater
    * than ''1'' and less than ''p'' is a generator. So we can fix the generator as ''3''.
    */
  val g = BigInt(3)

}