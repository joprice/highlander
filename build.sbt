import chrome.permissions.APIPermission
import chrome._
import Impl._

scalaVersion := "2.11.7"

val highlander = project.in(file("."))
  .enablePlugins(ScalaJSPlugin, ChromeSbtPlugin)
  .settings(
    scalaJSStage in Global := FastOptStage,
    emitSourceMaps in fullOptJS := true,
    persistLauncher := true,
    chromeManifest := ExtensionManifest(
      name = "Highlander",
      version = "0.1.0",
      manifestVersion = 2,
      background = Background(List(
        "deps.js",
        "main.js",
        "launcher.js"
      )),
      shortName = None,
      defaultLocale = None,
      description = None,
      offlineEnabled = true,
      permissions = Set(APIPermission.Tabs),
      icons = Map.empty,
      minimumChromeVersion = Some("23")
    ),
    libraryDependencies ++= Seq(
    "net.lullabyte" %%% "scala-js-chrome" % "0.2.0"
    )
  )

