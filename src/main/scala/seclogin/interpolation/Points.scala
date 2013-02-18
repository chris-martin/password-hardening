package seclogin.interpolation

import collection.immutable.Seq

case class Points(ps: Seq[Point]) {
  def toSeq: Seq[Point] = ps
  def n: Int = ps.length
  def x(i: Int): Rational = ps(i).x
  def y(i: Int): Rational = ps(i).y
  def truncate(k: Int): Points = Points(ps.slice(0, k))
}

object Points {

  implicit def pointSeq2points(x: Seq[Point]): Points = new Points(x)

  implicit def tupleSeq2points(x: Seq[(Rational, Rational)]): Points =
    new Points(x.map(p => p:Point).toSeq)

  implicit def points2seq(x: Points): Seq[Point] = x.toSeq

}
