import play.Project._

name := "play-webshop"

version := "1.0"

// resolvers += "Sonatype snapshots" at "https://oss.sonatype.org/content/repositories/snapshots"

libraryDependencies ++= Seq(
  jdbc,
  anorm,
  "com.typesafe.play" %% "play-slick" % "0.5.0.3-SNAPSHOT"
)

playScalaSettings

// val root = project
// 
// root.dependsOn(RootProject( uri("git://github.com/freekh/play-slick.git") ))
