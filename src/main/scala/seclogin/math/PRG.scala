package seclogin.math

import java.security.SecureRandom
import scala.util.Random

class PRG(key: Array[Byte] = Array(),
    secureRandom: SecureRandom = SecureRandom.getInstance("SHA1PRNG"))
    extends Random(secureRandom) {

  secureRandom.setSeed(key)

}
object PRG {

  def random(): java.util.Random = new PRG().self

  def random(key: Array[Byte]): java.util.Random = new PRG().self

}