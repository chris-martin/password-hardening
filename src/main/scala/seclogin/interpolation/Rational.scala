package seclogin.interpolation

import Rational._

case class Rational(a: BigInt, b: BigInt = 1) {

  def numerator: BigInt = a
  def demoninator: BigInt = b

  def withNumerator(x: BigInt): Rational = reduced(x, b)
  def withDemominator(x: BigInt): Rational = reduced(a, x)

  def inverse: Rational = reduced(b, a)

  def pow(exp: Int): Rational = reduced(a pow exp, b pow exp)

  def toBigInt: BigInt = a/b

}
 
object Rational {

  def reduced(a: BigInt, b: BigInt): Rational = {
    val gcd = (a gcd b) * b.signum
    Rational(a / gcd, b / gcd)
  }

  implicit def implicitFromBigInt(a: BigInt): Rational = Rational(a)
  implicit def implicitFromLong(a: Long): Rational = Rational(a)
  implicit def implicitFromInt(a: Int): Rational = Rational(a)

  implicit object RationalIsFractional extends Fractional[Rational] {
    def plus(x: Rational, y: Rational): Rational = reduced(x.a*y.b + y.a*x.b, x.b*y.b)
    def minus(x: Rational, y: Rational): Rational = x + negate(y)
    def times(x: Rational, y: Rational): Rational = reduced(x.a*y.a, x.b*y.b)
    def div(x: Rational, y: Rational): Rational = x * y.inverse
    def negate(x: Rational): Rational = reduced(-x.a, x.b)
    def fromInt(x: Int): Rational = Rational(x)
    def toInt(x: Rational): Int = x.toBigInt.toInt
    def toLong(x: Rational): Long = (x.a/x.b).toLong
    def toFloat(x: Rational): Float = x.a.toFloat / x.b.toFloat
    def toDouble(x: Rational): Double = x.a.toDouble / x.b.toDouble
    def compare(x: Rational, y: Rational): Int = x.a*y.b compare y.a*x.b
  }
 
}