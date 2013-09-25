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
  "net.fwbrasil" %% "activate-play" % "1.4-RC1" exclude("org.scala-stm", "scala-stm_2.10.0"),
  "net.fwbrasil" %% "activate-jdbc" % "1.4-RC1"
)

playScalaSettings

// val root = project
// 
// root.dependsOn(RootProject( uri("git://github.com/freekh/play-slick.git") ))
