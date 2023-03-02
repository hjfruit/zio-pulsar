# ZIO Pulsar

添加依赖：

Scala 3
```
libraryDependencies += "fc.xuanwu.star" %% "xuanwu-zio-pulsar" % NewVersion
```

Scala 2.13.6+ (sbt 1.5.x)
```
libraryDependencies += 
  ("fc.xuanwu.star" %% "xuanwu-zio-pulsar" % NewVersion).cross(CrossVersion.for2_13Use3)
```

项目类路径中需要这些依赖（ZIO项目只需要注意得有没有导入streams）：
```
libraryDependencies ++= Seq(
  "dev.zio" %% "zio"         % zioVersion,
  "dev.zio" %% "zio-streams" % zioVersion
)
```

例子：
```scala
object SingleMessageExample extends ZIOAppDefault:

  val pulsarClient = PulsarClient.live("localhost", 6650)
  // val pulsarClient = PulsarClient.live(""pulsar://localhost:6650,localhost:6651,localhost:6652"")

  val topic = "single-topic"

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