package seclogin.math

import scala.util.Random
import java.security.SecureRandom

/** Adapts a `Random` for generating primes.
  */
case class RandomPrime(r: Random = new SecureRandom) {

  /** @return a random `numbits`-bit prime `p`
    */
  def nextPrime(numbits: Int): BigInt =
    (
      Stream
      .continually { BigInt(numbits = numbits, rnd = r) }
      .filter { p => p.isProbablePrime(100) }
      .head
    )

  /** @return a random `numbits`-bit prime ''p'' such that ''2p+1'' is prime
    */
  def nextSophieGermainPrime(numbits: Int): BigInt =
    (
      Stream
      .continually { nextPrime(numbits) }
      .filter { p => ((p * 2) + 1).isProbablePrime(100) }
      .head
    )

  /** @return a random `numbits`-bit prime `q` such that ''q=2p+1'' where ''p'' is prime
    */
  def nextSafePrime(numbits: Int): BigInt =
    (nextSophieGermainPrime(numbits - 1) * 2) + 1

}
