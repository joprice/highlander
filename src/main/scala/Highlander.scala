
import chrome.tabs.Tabs
import chrome.tabs.bindings.{TabQuery, Tab, ChangeInfo, UpdateProperties}
//import chrome.tabs.bindings._
import scala.scalajs.js
import scala.concurrent.Future
import scalajs.concurrent.JSExecutionContext.Implicits.queue
import scala.scalajs.js.JSConverters._

object Highlander extends js.JSApp {

  // the url used when a tab is first open, before the url has been set
  val NewTab = "chrome://newtab/"

  def checkForExistingTab(id: Tab.Id, url: String): Future[Option[Tab]] = {
    Tabs.query(TabQuery(url = url: js.Any)).flatMap { tabs =>
      if (tabs.size > 1) {
        // take a tab, where that tab is not the newly opened one
        val ids = (tabs.flatMap(_.id.toOption) - id).tail :+ id
        Tabs.remove(ids).flatMap { _ =>
          Tabs.update(tabs.head.id, UpdateProperties(active = true))
            .map(_.toOption)
        }
      } else Future.successful(None)
    }
  }

  def main(): Unit = {
    Tabs.onUpdated.listen { case (id: Tab.Id, changeInfo: ChangeInfo, tab: Tab) =>
      changeInfo.url.filterNot(_ == NewTab)
        .foreach(checkForExistingTab(id, _))
    }
  }
}
