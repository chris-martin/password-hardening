package seclogin.interpolation

import java.math.{BigInteger⇒JBigInt}
import java.util.{List⇒JList}
import scala.collection.JavaConversions._

class InterpolationForJava {

  def yIntercept(xys: JList[JBigInt], q: JBigInt): JBigInt = {
    implicit val modQ = Mod(q)
    xys.grouped(2).map(xy => Point(xy(0), xy(1))).toSeq.yIntercept.bigInteger
  }

}