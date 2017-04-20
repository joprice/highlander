import chrome.permissions.Permission
import chrome._

scalaVersion := "2.11.8"

val highlander = project.in(file("."))
  .enablePlugins(ScalaJSPlugin, ChromeSbtPlugin)
  .settings(
    name := "Highlander",
    version := "0.1.0",
    scalaJSStage in Global := FastOptStage,
    emitSourceMaps in fullOptJS := true,
    persistLauncher := true,
    //scalaJSUseMainModuleInitializer := true,
    chromeManifest := new ExtensionManifest {
      val name = Keys.name.value
      val version = Keys.version.value
      override val manifestVersion = 2
      val background = Background(List(
        "dependencies.js",
        "main.js",
        "launcher.js"
      ))
      override val permissions = Set[Permission](
        Permission.API.Tabs,
        Permission.API.Notifications
      )
      override val minimumChromeVersion = Some("23")
    },
    libraryDependencies ++= Seq(
    "net.lullabyte" %%% "scala-js-chrome" % "0.4.0"
    )
  )

