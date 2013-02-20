package seclogin.interpolation

import scala.collection.JavaConversions._
import org.scalatest._
import java.math.BigInteger

class InterpolationForJavaSpec extends FreeSpec {

  "tiny interpolation" in {
    val i = new InterpolationForJava().yIntercept(
      Seq(
        BigInteger.valueOf(2), BigInteger.valueOf(2),
        BigInteger.valueOf(4), BigInteger.valueOf(3)
      ),
      BigInteger.valueOf(11)
    )
    assert(i == BigInteger.valueOf(1))
  }

}