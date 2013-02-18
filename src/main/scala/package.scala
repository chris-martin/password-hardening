package object seclogin {

  implicit def points2interpolation(x: Points): Interpolation = new Interpolation(x)

}