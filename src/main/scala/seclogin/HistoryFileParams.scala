package seclogin

case class HistoryFileParams(
  maxNrOfEntries: Int,
  nrOfFeatures: Int
) {
  def h: Int = maxNrOfEntries
  def m: Int = nrOfFeatures
}
