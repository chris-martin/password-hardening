package seclogin.math

import scala.collection.immutable.Seq
import scala.math.BigInt

import java.math.{BigInteger⇒JBigInt}
import java.util.{List⇒JList}
import scala.collection.JavaConversions._

/** Interpolation for the unique minimal-degree polynomial
  * ''f : ℤ,,q,, → ℤ,,q,,'' containing all of the `points`.
  * @param points ''(x, y)'' pairs that reside in ''f''
  * @param q size of the field over which ''f'' is defined
  */
case class Interpolation(points: Seq[Point])(implicit q: Mod) extends Points {

  /** A Java-friendly constructor.
    * @param xys ''(x, y)'' pairs that reside in ''f'', given as
    * ''(x_1, y_1, x_2, x_2, ..., x_n, y_n)''
    * @param q size of the field over which ''f'' is defined
    */
  def this(xys: JList[JBigInt], q: JBigInt) =
    this(Seq(xys.grouped(2).map(xy => Point(xy(0), xy(1))).toSeq:_*))(Mod(q))

  /** @return the value of the interpolated polynomial evaluated at `0`
    */
  def yIntercept: BigInt = {
    val terms = for (i ← 0 until n) yield { (y(i) * λ(i)) mod q }
    terms.sum mod q
  }

  /** @return the `i`^th^ Lagrange coefficient
    */
  def λ(i: Int): BigInt = {
    val terms = for (j ← 0 until n; if i != j) yield { q.divide(x(j), x(j) - x(i)) }
    terms.product mod q
  }

}
