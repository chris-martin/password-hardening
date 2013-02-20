package seclogin

case class MeasurementStats(mean: Double, stDev: Double) {
  def mu: Double = mean
  def sigma: Double = stDev
}
