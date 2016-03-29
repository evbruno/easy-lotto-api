import sbt.Keys._

enablePlugins(JavaAppPackaging)

lazy val commonSettings = Seq(
  name          := "easy-lotto-api",
  organization  := "com.github.evbruno",
  version       := "1.0",
  scalaVersion  := "2.11.7"
)

scalacOptions := Seq("-unchecked", "-deprecation", "-encoding", "utf8", "-Xlint", "-feature")

//fork in run := true

libraryDependencies ++= {
    val akkaV = "2.3.14"
    //  val akkaStreamV = "1.0"
    val akkaStreamV = "2.0"
    val scalaTestV = "2.2.5"
    val casbahVersion = "2.8.2"
    Seq(
        "com.typesafe.akka" %% "akka-actor" % akkaV,
        "com.typesafe.akka" %% "akka-stream-experimental" % akkaStreamV,
        "com.typesafe.akka" %% "akka-http-core-experimental" % akkaStreamV,
        "com.typesafe.akka" %% "akka-http-experimental" % akkaStreamV,
        "com.typesafe.akka" %% "akka-http-spray-json-experimental" % akkaStreamV,
        "com.typesafe.akka" %% "akka-http-testkit-experimental" % akkaStreamV,

        "org.scala-lang.modules" 	%% "scala-xml" % "1.0.5",

        "org.mongodb"	 	%% "casbah-core" 	% casbahVersion,
        "ch.qos.logback" 	% "logback-classic" % "1.1.3",
        "commons-io" 		% "commons-io" 		% "2.4",
        "net.ruippeixotog" 	%% "scala-scraper" 	% "0.1.1",

        "org.scalatest" %% "scalatest" % scalaTestV % "test"
    )
}

Revolver.settings

lazy val root = (project in file(".")).
    settings(commonSettings)

lazy val jobs = project.dependsOn(root).
    settings(
        commonSettings
    )
