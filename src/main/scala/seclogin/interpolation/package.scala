package seclogin

package object interpolation {

  implicit def points2interpolation(x: Points): Interpolation = new Interpolation(x)

}