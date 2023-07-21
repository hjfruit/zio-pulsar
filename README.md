ZIO Pulsar
---

![CI][Badge-CI] [![Nexus (Snapshots)][Badge-Snapshots]][Link-Snapshots]


[Badge-CI]: https://github.com/hjfruit/zio-pulsar/actions/workflows/scala.yml/badge.svg
[Badge-Snapshots]: https://img.shields.io/nexus/s/fc.xuanwu.star/zio-pulsar_3?server=https%3A%2F%2Fs01.oss.sonatype.org
[Link-Snapshots]: https://s01.oss.sonatype.org/content/repositories/snapshots/fc/xuanwu/star/zio-pulsar_3

## 添加依赖

Scala 3
```
libraryDependencies += "fc.xuanwu.star" %% "xuanwu-zio-pulsar" % <latest version>
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

## 例子1
```scala
object SingleMessageExample extends ZIOAppDefault:

  // 注意：不要重复构建本对象，保持一个实例即可！
  lazy val pulsarClient = PulsarClient.live("localhost", 6650)
  // val pulsarClient = PulsarClient.live(""pulsar://localhost:6650,localhost:6651,localhost:6652"")

  val topic = "single-topic"

  // 为了避免频繁创建client，client的Scope和consumer/producer的Scope不应该都使用Scope.default
  // client应该是个长期/按需使用的对象，而consumer/producer是用完即逝的
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

## 例子2

> 略微封装，仅适合最基本使用场景
```scala
  lazy val consumer = PulsarClientF
    .consumeF(
      setting.respTopicName,
      Subscription(setting.respSubscribeName, Shared)
        .withInitialPosition(SubscriptionInitialPosition.Earliest)
    )
    .mapError(f => f.getCause)
  consumer.flatMap(_.receive(10, TimeUnit.SECONDS))

  val producer = PulsarClientF.productF(setting.reqTopicName)
  producer.flatMap(_.send(JacksonUtils.writeValueAsString(req)))
```