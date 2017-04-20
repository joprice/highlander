
import chrome.events.{EventSource, Subscription}
import chrome.notifications.Notifications
import chrome.notifications.bindings.{Button, NotificationOptions, TemplateType}
import chrome.windows.Windows
import chrome.tabs.Tabs
import chrome.tabs.bindings.{ChangeInfo, Tab, TabQuery, UpdateProperties}
import chrome.windows.bindings.{UpdateOptions, Window}
import scala.scalajs.js
import scala.concurrent.Future
import scalajs.concurrent.JSExecutionContext.Implicits.queue

object Highlander extends js.JSApp {

  val DuplicateTabID = "duplicate-tab"

  // the url used when a tab is first open, before the url has been set
  val NewTab = "chrome://newtab/"

  def focusWindow(id: Window.Id): Future[Window] =
    Windows.get(id).flatMap { window =>
      if (!window.focused)
        Windows.update(id, UpdateOptions(focused = true))
      else Future.successful(window)
    }

  def focusTab(tab: Tab): Future[Option[Tab]] =
    focusWindow(tab.windowId).flatMap { _ =>
      Tabs.update(tab.id, UpdateProperties(active = true))
        .map(_.toOption)
    }

  //TODO: hashes seems to be ignored by this query - trim them?
  def tabsWithUrl(url: String): Future[js.Array[Tab]] = {
    // we query without hash in order to get urls that include hashes, then discard the ones that
    // don't match exactly
    val normalizedUrl = url.replaceFirst("#.*$", "")
    Tabs.query(TabQuery(url = normalizedUrl: js.Any)).map { results =>
      results.filter(_.url.exists(_ == url))
    }
  }

  def notifyOnce[A](source: EventSource[A])(f: A => Unit): Unit = {
    // This is lazy and typed to get recursive definition errors. It is safe since the reference is in a callback
    lazy val sub: Subscription = source.listen { value =>
      f(value)
      sub.cancel()
    }
    sub
  }

  def checkForExistingTab(id: Tab.Id, url: String): Unit = {
    tabsWithUrl(url).foreach { tabs =>
      val otherTabs = tabs.filter(_.id.exists(_ != id))
      otherTabs.headOption.foreach { first =>
        val rest = otherTabs.drop(1)
        val ids = id +: rest.flatMap(_.id.toOption)
        val singular = rest.size <= 1
        Notifications.create(
          NotificationOptions(
            `type` = TemplateType.BASIC,
            title = "Highlander",
            message = s"${if (singular) "Another tab" else s"${ids.size} other tabs"} with that url already ${if (singular) "exists" else "exist"}!",
            iconUrl = "data:image/jpeg;base64,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",
            contextMessage = url,
            buttons = js.Array(
              Button("There can only be one!"),
              Button(s"Let ${if (singular) "it" else "them"} live.")
            )
          ),
          Some(DuplicateTabID)
        ).recover {
          case ex: Throwable =>
            println(s"Failed creating notification $ex")
        }

        notifyOnce(Notifications.onButtonClicked) {
          case (DuplicateTabID, 0) =>
            for {
              _ <- Notifications.clear(DuplicateTabID)
              _ <- focusTab(first)
              _ <- Tabs.remove(ids)
            } yield ()
          case (DuplicateTabID, 1) =>
            Notifications.clear(DuplicateTabID)
          case _ => println("Ignoring unknown notification")
        }
      }
    }
  }

  def main(): Unit = {
    Tabs.onUpdated.listen { case (id: Tab.Id, changeInfo: ChangeInfo, tab: Tab) =>
      //TODO: check status for complete before querying?
      // changeInfo.status
      changeInfo.url.filterNot(_ == NewTab)
        .foreach {
          checkForExistingTab(id, _)
        }
    }
  }
}
