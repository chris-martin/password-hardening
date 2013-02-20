package seclogin

case class HistoryFileParams(
  maxNrOfEntries: Int,
  nrOfFeatures: Int
) {
  def H: Int = maxNrOfEntries
  def M: Int = nrOfFeatures
}
