package seclogin.math

import scala.math.BigInt

/** Represents ''ℤ,,q,,'', the field of integers ''[0, q)''.
  * @param q the size of the field
  */
case class Mod(q: BigInt) {

  /** @return ''`a`/`b` (mod `q`)''
    */
  def divide(a: BigInt, b: BigInt): BigInt = a * (b modInverse q) mod q

  /** @return ''`base`^`exp`^ (mod `q`)''
    */
  def pow(base: BigInt, exp: BigInt): BigInt = base modPow (exp, q)

}

object Mod {
  implicit def int2Mod(x: Int): Mod = Mod(x)
  implicit def long2Mod(x: Long): Mod = Mod(x)
  implicit def bigInt2Mod(x: BigInt): Mod = Mod(x)
  implicit def mod2BigInt(x: Mod): BigInt = x.q
}
