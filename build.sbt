import sbt.Keys._

enablePlugins(JavaAppPackaging)

lazy val commonSettings = Seq(
  organization  := "com.github.evbruno",
  version       := "1.0-SNAPSHOT",
  scalaVersion  := "2.11.8"
)

scalacOptions := Seq("-unchecked", "-deprecation", "-encoding", "utf8", "-Xlint", "-feature")

//fork in run := true

libraryDependencies ++= {
    val akkaStreamV = "2.0.3"
    val scalaTestV = "2.2.5"
    val casbahVersion = "2.8.2"
    Seq(
        "com.typesafe.akka" %% "akka-stream-experimental" % akkaStreamV,
        "com.typesafe.akka" %% "akka-http-core-experimental" % akkaStreamV,
        "com.typesafe.akka" %% "akka-http-experimental" % akkaStreamV,
        "com.typesafe.akka" %% "akka-http-spray-json-experimental" % akkaStreamV,
        "com.typesafe.akka" %% "akka-http-testkit-experimental" % akkaStreamV,

        "org.mongodb"	 	%% "casbah-core" 	% casbahVersion,
        "ch.qos.logback" 	% "logback-classic" % "1.1.3",
        "commons-io" 		% "commons-io" 		% "2.4",
        "net.ruippeixotog" 	%% "scala-scraper" 	% "1.0.0",

		"com.github.evbruno" %% "agatetepe" % "1.0.1",

        // "org.scala-lang.modules" %% "scala-xml" % "1.0.5",
        // "org.scala-lang.modules" %% "scala-parser-combinators" % "1.0.4",

        "org.scalatest" %% "scalatest" % scalaTestV % "test",

        "pl.project13.scala" %% "rainbow" % "0.2"
    )
}

Revolver.settings

resolvers ++= Seq(
        Resolver.sonatypeRepo("public"),
        Resolver.sonatypeRepo("snapshots")
)

lazy val root = (project in file("."))
   .settings(commonSettings: _*)
   .settings(
        name := "easy-lotto-api"
    )

lazy val jobs = project.dependsOn(root)
   .settings(commonSettings: _*)
   .settings(
        name := "easy-lotto-api-jobs",
        excludeDependencies += "com.typesafe.akka" %% "akka-stream-experimental",
        excludeDependencies += "com.typesafe.akka" %% "akka-http-core-experimental",
        excludeDependencies += "com.typesafe.akka" %% "akka-http-experimental",
        excludeDependencies += "com.typesafe.akka" %% "akka-http-testkit-experimental",
        excludeDependencies += "com.typesafe.akka" %% "akka-http-spray-json-experimental"
    )