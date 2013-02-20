package seclogin

case class IndecipherableHistoryFileException(e: Throwable) extends Exception(e) {

  def this() = this(null)

}
