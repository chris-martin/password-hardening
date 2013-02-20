package seclogin

/** @param responseMean response mean
  * @param stDevMultiplier standard deviation multiplier
  */
case class MeasurementParams(responseMean: Double, stDevMultiplier: Double) {
  def t: Double = responseMean
  def k: Double = stDevMultiplier
}
