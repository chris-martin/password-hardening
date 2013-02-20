package seclogin.math

import java.security.SecureRandom
import scala.util.Random

/** This class mostly just serves as a marker type, indicating that the `Random`
  * implementation is cryptographically secure.
  * @param key Used to seed `secureRandom` when this `PRG` is constructed.
  * Defaults to empty array.
  * @param self The backing random implementation. Must be a cryptographically
  * secure PRG. Defaults to Java's SHA-1 algorithm.
  */
class PRG(key: Array[Byte] = Array(),
    self: SecureRandom = SecureRandom.getInstance("SHA1PRNG"))
    extends Random(self) {

  self.setSeed(key)

}
object PRG {

  /** Java-friendly method for creating a cryptographically secure PRG with a default key.
    */
  def random(): java.util.Random = new PRG().self

  /** Java-friendly method for creating a cryptographically secure PRG with a given `key`.
    */
  def random(key: Array[Byte]): java.util.Random = new PRG().self

}