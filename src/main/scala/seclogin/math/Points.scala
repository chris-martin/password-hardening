package seclogin.math

import scala.collection.immutable.Seq
import scala.math.BigInt

trait Points {

  def points: Seq[Point]

  def n = points.size

  def x(i: Int): BigInt = points(i).x

  def y(i: Int): BigInt = points(i).y

}
