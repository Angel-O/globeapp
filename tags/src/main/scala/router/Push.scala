package router

import org.scalajs.dom.document

private object Push {
  def push(path: String = "") = document.location.hash = path
  def apply(baseURI: String) = new Push(baseURI)
}
protected case class Push private (baseURI: String) {
  def apply(path: String) = Push.push(s"$baseURI$path")
}

trait HashUpdater {
  private val navigator = Push.apply _
  val baseUrl: Option[String] = None
  private def getUrl = baseUrl.getOrElse("")
  private lazy val navigateTo = navigator(getUrl)

  def push(path: String) =
    if (path == getUrl) navigateTo("/") else navigateTo(path)
}
