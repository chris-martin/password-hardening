package seclogin.math

import scala.math.BigInt
import java.util.Random
import java.math.BigInteger

/** Represents ''ℤ,,q,,'', the field of integers ''[0, q)''.
  * @param q the size of the field
  */
case class Mod(q: BigInt) {

  /** A Java-friendly constructor.
    * @param q the size of the field
    */
  def this(q: BigInteger) =
    this(BigInt.apply(q))

  /** @return ''`a`/`b` (mod `q`)''
    */
  def divide(a: BigInt, b: BigInt): BigInt = a * (b modInverse q) mod q

  /** @return ''`base`^`exp`^ (mod `q`)''
    */
  def pow(base: BigInt, exp: BigInt): BigInt = base modPow (exp, q)

  /** @return a random element of ''ℤ,,q,,'' not equal to the given value.
    */
  def randomElement(random: Random): BigInt = {
    var candidate: BigInt = null
    do {
      candidate = BigInt(q.bitLength, random)
    } while (candidate >= q)
    candidate
  }

  /** @return a random element of ''ℤ,,q,,'' not equal to the given value.
    */
  def randomElementNotEqualTo(forbiddenValue: BigInt, random: Random): BigInt = {
    var candidate: BigInt = null
    do {
      candidate = randomElement(random)
    } while (candidate equals forbiddenValue)
    candidate
  }

}

object Mod {
  implicit def int2Mod(x: Int): Mod = Mod(x)
  implicit def long2Mod(x: Long): Mod = Mod(x)
  implicit def bigInt2Mod(x: BigInt): Mod = Mod(x)
  implicit def mod2BigInt(x: Mod): BigInt = x.q
}
