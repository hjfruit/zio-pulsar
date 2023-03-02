val zioVersion    = "2.0.7"
val pulsarVersion = "2.10.0"

ThisBuild / resolvers += Resolver.mavenLocal
ThisBuild / resolvers += "fc".at("https://jfrog-artifactory.hjgpscm.com/artifactory/public")
ThisBuild / resolvers += "sonatype snaphots" at "https://oss.sonatype.org/content/repositories/snapshots/"

inThisBuild(
  List(
    scalaVersion := "3.2.0"
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
      "dev.zio"             %% "zio-json"                    % "0.4.2"       % Provided,
      "com.sksamuel.avro4s" %% "avro4s-core"                 % "5.0.3",
      "org.apache.pulsar"    % "pulsar-client"               % pulsarVersion,
      "ch.qos.logback"       % "logback-classic"             % "1.4.5",
      "dev.zio"             %% "zio-test"                    % zioVersion    % Test,
      "dev.zio"             %% "zio-test-sbt"                % zioVersion    % Test,
      "dev.zio"             %% "zio-test-junit"              % zioVersion    % Test,
      "dev.zio"             %% "zio-test-magnolia"           % zioVersion    % Test,
      "org.testcontainers"   % "pulsar"                      % "1.17.5"      % Test,
      "com.dimafeng"        %% "testcontainers-scala-pulsar" % "0.40.12"     % Test,
      "org.apache.pulsar"    % "pulsar-client-admin-api"     % pulsarVersion % Test,
      "org.apache.pulsar"    % "pulsar-client-admin"         % pulsarVersion % Test
    ),
    testFrameworks := Seq(new TestFramework("zio.test.sbt.ZTestFramework"))
  )
  .settings(Publishing.publishing)

lazy val examples = project
  .in(file("examples"))
  .settings(
    publish / skip := true,
    moduleName     := "examples",
    libraryDependencies ++= Seq(
      // "dev.zio" %% "zio-logging" % "0.5.6",
      "dev.zio"             %% "zio"             % zioVersion,
      "dev.zio"             %% "zio-streams"     % zioVersion,
      "dev.zio"             %% "zio-json"        % "0.4.2",
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
