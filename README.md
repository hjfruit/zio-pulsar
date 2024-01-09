ZIO Pulsar
---

![CI][Badge-CI] [![Nexus (Snapshots)][Badge-Snapshots]][Link-Snapshots] [![Sonatype Nexus (Releases)][Badge-Release]][Link-Release]


[Badge-CI]: https://github.com/hjfruit/zio-pulsar/actions/workflows/scala.yml/badge.svg
[Badge-Snapshots]: https://img.shields.io/nexus/s/io.github.jxnu-liguobin/zio-pulsar_3?server=https%3A%2F%2Foss.sonatype.org
[Link-Snapshots]: https://oss.sonatype.org/content/repositories/snapshots/io/github/jxnu-liguobin/zio-pulsar_3/
[Link-Release]: https://oss.sonatype.org/content/repositories/public/io/github/jxnu-liguobin/zio-pulsar_3/
[Badge-Release]: https://img.shields.io/nexus/r/io.github.jxnu-liguobin/zio-pulsar_3?server=https%3A%2F%2Foss.sonatype.org


## Dependency

Scala 3
```
libraryDependencies += "io.github.jxnu-liguobin" %% "zio-pulsar" % <latest version>
```

Scala 2.13.6+ (sbt 1.5.x)
```
libraryDependencies += 
  ("io.github.jxnu-liguobin" %% "zio-pulsar" % NewVersion).cross(CrossVersion.for2_13Use3)
```

These dependencies are required in the project classpath (ZIO projects only need to pay attention to whether they have imported zio-streams):
```
libraryDependencies ++= Seq(
  "dev.zio" %% "zio"         % zioVersion,
  "dev.zio" %% "zio-streams" % zioVersion
)
```

## Example 1
```scala
object SingleMessageExample extends ZIOAppDefault:

  // Note: Do not duplicate the construction of this object, just keep one instance!
  lazy val pulsarClient = PulsarClient.live("localhost", 6650)
  // val pulsarClient = PulsarClient.live(""pulsar://localhost:6650,localhost:6651,localhost:6652"")

  val topic = "single-topic"

  // The client should be a long-term/on-demand object, while the consumer/producer is perishable after use
  val app: ZIO[PulsarClient & Scope, PulsarClientException, Unit] =
    for
      builder  <- ConsumerBuilder.make(JSchema.STRING)
      consumer <- builder
                    .topic(topic)
                    .subscription(Subscription("my-subscription", SubscriptionType.Shared))
                    .build
      producer <- Producer.make(topic, JSchema.STRING)
      _        <- producer.send("Hello!")
      m        <- consumer.receive
      _ = println(m.getValue)
    yield ()

  override def run = app.provideLayer(pulsarClient ++ Scope.default).exitCode
```

## Example 2

Injected using constructor:
```scala
import zio.pulsar.ZioPulsar

final case class UserService(zioPulsar: ZioPulsar) {

  def sendPulsar(): ZIO[Scope, PulsarClientException, Message[String]] =
    zioPulsar
      .consumerBuilder(JSchema.STRING)
      .flatMap(
        _.topic(topic)
          .subscription(
            Subscription(
              name = "zio-subscription",
              `type` = SubscriptionType.Shared
            )
          )
          .build
      )
      .flatMap(_.receive(10, TimeUnit.SECONDS))
}
```