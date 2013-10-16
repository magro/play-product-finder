import play.Project._

name := "play-webshop"

version := "1.0"

// resolvers += "Sonatype snapshots" at "https://oss.sonatype.org/content/repositories/snapshots"
resolvers ++= Seq(
  "Local Maven Repository" at "file://"+Path.userHome+"/.m2/repository",
  "fwbrasil.net" at "http://fwbrasil.net/maven/"
)

libraryDependencies ++= Seq(
  jdbc,
  anorm,
  "com.typesafe.play" %% "play-slick" % "0.5.0.3-SNAPSHOT",
  "net.fwbrasil" %% "activate-play" % "1.4.1" exclude("org.scala-stm", "scala-stm_2.10.0"),
  "net.fwbrasil" %% "activate-jdbc" % "1.4.1",
  "net.fwbrasil" %% "activate-jdbc-async" % "1.4.1",
  "postgresql" % "postgresql" % "9.1-901-1.jdbc4",
  "nu.validator.htmlparser" % "htmlparser" % "1.4",
  "org.scalesxml" %% "scales-xml" % "0.6.0-M1",
  // and additionally use these for String based XPaths
  "org.scalesxml" %% "scales-jaxen" % "0.6.0-M1" intransitive(), 
  "jaxen" % "jaxen" % "1.1.3" intransitive()
)

playScalaSettings

// From activate-example-play-async
// Keys.fork in Test := false

// val root = project
// 
// root.dependsOn(RootProject( uri("git://github.com/freekh/play-slick.git") ))
