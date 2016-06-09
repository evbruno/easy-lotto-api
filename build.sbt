import sbt.Keys._

enablePlugins(JavaAppPackaging)

// configs

lazy val versions = new {
    val logback = "1.1.3"
    val akka = "2.4.7"
    val scalaTestV = "2.2.5"
    val casbah = "3.1.1"
    val commonsIO = "2.4"
    val scraper = "1.0.0"
    val agatetepe = "1.0.1"
    val scalaTest = "2.2.5"
    val rainbow = "0.2"
}

lazy val commonSettings = Seq(
    organization  := "com.github.evbruno",
    version       := "1.0-SNAPSHOT",
    scalaVersion  := "2.11.8",
    libraryDependencies ++= Seq(
        "ch.qos.logback"     % "logback-classic" % versions.logback,
        "pl.project13.scala" %% "rainbow"        % versions.rainbow,
        "org.scalatest"      %% "scalatest"      % versions.scalaTest % "test"
    )
)

// projects

lazy val root = (project in file("."))
    .dependsOn(api)
    .settings(commonSettings: _*)
    .settings(name := "easy-lotto-api")
    .settings(libraryDependencies ++= Seq(
        "com.typesafe.akka" %% "akka-stream"                       % versions.akka,
        "com.typesafe.akka" %% "akka-http-core"                    % versions.akka,
        "com.typesafe.akka" %% "akka-http-experimental"            % versions.akka,
        "com.typesafe.akka" %% "akka-http-spray-json-experimental" % versions.akka,
        "com.typesafe.akka" %% "akka-http-testkit"                 % versions.akka
    ))

lazy val api = project
    .settings(commonSettings: _*)
    .settings(name := "easy-lotto-api-api")
    .settings(libraryDependencies ++= Seq(
        "org.mongodb" %% "casbah-core" % versions.casbah
    ))

lazy val jobs = project.dependsOn(api)
    .settings(commonSettings: _*)
    .settings(name := "easy-lotto-api-jobs")
    .settings(libraryDependencies ++= Seq(
        "org.mongodb"        %% "casbah-core"   % versions.casbah,
        "commons-io"         % "commons-io"     % versions.commonsIO,
        "net.ruippeixotog"   %% "scala-scraper" % versions.scraper,
        "com.github.evbruno" %% "agatetepe"     % versions.agatetepe
    ))

// compile and stuff

scalacOptions := Seq("-unchecked", "-deprecation", "-encoding", "utf8", "-Xlint", "-feature")

//fork in run := true

Revolver.settings

resolvers ++= Seq(
    Resolver.sonatypeRepo("public"),
    Resolver.sonatypeRepo("snapshots")
)
