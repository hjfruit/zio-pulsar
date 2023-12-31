package examples

import java.io.IOException

import zio.*
import zio.pulsar.*
import zio.stream.*

import org.apache.pulsar.client.api.{ PulsarClientException, Schema as JSchema }

object StreamingExample extends ZIOAppDefault:

  val pulsarClient = PulsarClient.live("localhost", 6650)

  val topic = "streaming-topic"

  val producer: ZIO[PulsarClient & Scope, PulsarClientException, Unit] =
    for
      sink  <- Producer.make(topic, JSchema.STRING).map(_.asSink)
      stream = zio.stream.ZStream.fromIterable(0 to 100).map(i => s"Message $i")
      _     <- stream.run(sink)
    yield ()

  val consumer: ZIO[PulsarClient with Scope, IOException, Unit] =
    for
      builder  <- ConsumerBuilder.make(JSchema.STRING)
      consumer <- builder
                    .subscription(Subscription("my-subscription", SubscriptionType.Exclusive))
                    .topic(topic)
                    .build
      _        <- consumer.receiveStream.take(10).foreach { a =>
                    Console.printLine(a.getValue) *> consumer.acknowledge(a.getMessageId)
                  }
    yield ()

  val app =
    for
      f <- consumer.fork
      _ <- producer
      _ <- f.join
    yield ()

  override def run = app.provideLayer(pulsarClient ++ Scope.default).exitCode
