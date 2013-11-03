import play.Project._
import com.typesafe.sbteclipse.core.EclipsePlugin.EclipseKeys

name := "play-product-finder"

version := "1.0"

// resolvers += "Sonatype snapshots" at "https://oss.sonatype.org/content/repositories/snapshots"
resolvers ++= Seq(
  "Local Maven Repository" at "file://"+Path.userHome+"/.m2/repository",
  "fwbrasil.net" at "http://fwbrasil.net/maven/"
)

resolvers += "linter" at "http://hairyfotr.github.io/linteRepo/releases"

addCompilerPlugin("com.foursquare.lint" %% "linter" % "0.1-SNAPSHOT")

libraryDependencies ++= Seq(
  jdbc,
  anorm,
  "net.fwbrasil" %% "activate-play" % "1.4.2" exclude("org.scala-stm", "scala-stm_2.10.0"),
  "net.fwbrasil" %% "activate-jdbc-async" % "1.4.2",
  "nu.validator.htmlparser" % "htmlparser" % "1.4",
  "org.scalesxml" %% "scales-xml" % "0.6.0-M1",
  // and additionally use these for String based XPaths
  "org.scalesxml" %% "scales-jaxen" % "0.6.0-M1" intransitive(), 
  "jaxen" % "jaxen" % "1.1.3" intransitive(),
  "org.joda" % "joda-money" % "0.9"
)

playScalaSettings

requireJs += "main.js"

requireNativePath := Some("/usr/local/lib/node_modules/requirejs/bin/r.js")

EclipseKeys.preTasks := Seq() // don't run compile before eclipse

EclipseKeys.withSource := true

// From activate-example-play-async
// Keys.fork in Test := false

// val root = project
// 
// root.dependsOn(RootProject( uri("git://github.com/freekh/play-slick.git") ))
