
import chrome.windows.Windows
import chrome.tabs.Tabs
import chrome.tabs.bindings.{TabQuery, Tab, ChangeInfo, UpdateProperties}
import chrome.windows.bindings.{Window, UpdateOptions}
import scala.scalajs.js
import scala.concurrent.Future
import scalajs.concurrent.JSExecutionContext.Implicits.queue
import scala.scalajs.js.JSConverters._

object Highlander extends js.JSApp {

  // the url used when a tab is first open, before the url has been set
  val NewTab = "chrome://newtab/"

  def focusWindow(id: Window.Id): Future[Window] = {
    Windows.get(id).flatMap { window =>
      if (!window.focused)
        Windows.update(id, UpdateOptions(focused = true))
      else Future.successful(window)
    }
  }

  def focusTab(tab: Tab): Future[Option[Tab]] = {
    focusWindow(tab.windowId).flatMap { window =>
      Tabs.update(tab.id, UpdateProperties(active = true))
        .map(_.toOption)
    }
  }

  def tabsWithUrl(url: String): Future[js.Array[Tab]] = Tabs.query(TabQuery(url = url: js.Any))

  def checkForExistingTab(id: Tab.Id, url: String): Future[Option[Tab]] = {
    tabsWithUrl(url).flatMap { tabs =>
      if (tabs.size > 1) {
        // take a tab, where that tab is not the newly opened one
        val ids = (tabs.flatMap(_.id.toOption) - id).tail :+ id
        focusTab(tabs.head).map { tab =>
          Tabs.remove(ids)
          tab
        }
      } else Future.successful(None)
    }
  }

  def main(): Unit = {
    Tabs.onUpdated.listen { case (id: Tab.Id, changeInfo: ChangeInfo, tab: Tab) =>
      changeInfo.url.filterNot(_ == NewTab)
        .foreach {
          checkForExistingTab(id, _)
        }
    }
  }
}
