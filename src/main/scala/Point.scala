package seclogin

case class Point(x: Rational, y: Rational) {

  def toTuple: (Rational, Rational) = (x, y)

}

object Point {

  implicit def tuple2point(t: (Rational, Rational)): Point = Point(t._1, t._2)

  implicit def point2tuple(p: Point): (Rational, Rational) = p.toTuple

}