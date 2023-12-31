val zioVersion    = "2.0.13"
val pulsarVersion = "2.11.0"
val scala3Version = "3.3.1"

inThisBuild(
  List(
    scalaVersion     := scala3Version,
    homepage         := Some(url("https://github.com/jczuchnowski/zio-pulsar/")),
    licenses         := List("BSD 2-Clause" -> url("https://opensource.org/licenses/BSD-2-Clause")),
    organization     := "io.github.jxnu-liguobin",
    organizationName := "FC Xuanwu",
    developers       := List(
      Developer(
        "jczuchnowski",
        "Jakub Czuchnowski",
        "jakub.czuchnowski@gmail.com",
        url("https://github.com/jczuchnowski")
      ),
      Developer(
        id = "jxnu-liguobin",
        name = "梦境迷离",
        email = "dreamylost@outlook.com",
        url = url("https://github.com/jxnu-liguobin")
      )
    )
  )
)

addCommandAlias("fmt", "all scalafmtSbt scalafmt test:scalafmt")
addCommandAlias("check", "all scalafmtSbtCheck scalafmtCheck test:scalafmtCheck")

lazy val core = project
  .in(file("core"))
  .settings(
    name           := "zio-pulsar",
    libraryDependencies ++= Seq(
      "dev.zio"             %% "zio"                         % zioVersion    % Provided,
      "dev.zio"             %% "zio-streams"                 % zioVersion    % Provided,
      "dev.zio"             %% "zio-json"                    % "0.5.0"       % Provided,
      "com.sksamuel.avro4s" %% "avro4s-core"                 % "5.0.3",
      "org.apache.pulsar"    % "pulsar-client"               % pulsarVersion,
      "ch.qos.logback"       % "logback-classic"             % "1.4.5",
      "dev.zio"             %% "zio-test"                    % zioVersion    % Test,
      "dev.zio"             %% "zio-test-sbt"                % zioVersion    % Test,
      "dev.zio"             %% "zio-test-junit"              % zioVersion    % Test,
      "dev.zio"             %% "zio-test-magnolia"           % zioVersion    % Test,
      "org.testcontainers"   % "pulsar"                      % "1.17.6"      % Test,
      "com.dimafeng"        %% "testcontainers-scala-pulsar" % "0.40.17"     % Test,
      "org.apache.pulsar"    % "pulsar-client-admin-api"     % pulsarVersion % Test,
      "org.apache.pulsar"    % "pulsar-client-admin"         % pulsarVersion % Test
    ),
    testFrameworks := Seq(new TestFramework("zio.test.sbt.ZTestFramework"))
  )

lazy val examples = project
  .in(file("examples"))
  .settings(
    publish / skip := true,
    moduleName     := "examples",
    libraryDependencies ++= Seq(
      // "dev.zio" %% "zio-logging" % "0.5.6",
      "dev.zio"             %% "zio"             % zioVersion,
      "dev.zio"             %% "zio-streams"     % zioVersion,
      "dev.zio"             %% "zio-json"        % "0.5.0",
      "com.sksamuel.avro4s" %% "avro4s-core"     % "5.0.3",
      "ch.qos.logback"       % "logback-classic" % "1.4.5"
    )
  )
  .dependsOn(core)

lazy val `zio-pulsar` = project
  .in(file("."))
  .settings(
    publish / skip := true
  )
  .aggregate(
    core,
    examples
  )
