import java.io.IOException

import zio.*
import zio.pulsar.*

import org.apache.pulsar.client.api.Schema as JSchema

object PropertiesExample extends ZIOAppDefault:

  val pulsarClient = PulsarClient.live("localhost", 6650)

  val topic = "properties-topic"

  val app: ZIO[PulsarClient with Scope, IOException, Unit] =
    for
      builder        <- ConsumerBuilder.make(JSchema.STRING)
      consumer       <- builder
                          .topic(topic)
                          .loadConf(Consumer.consumerName("hello-consumer"))
                          .subscription(Subscription("my-subscription", SubscriptionType.Shared))
                          .build
      _              <- Console.printLine(Consumer.consumerName("hello-consumer").name == "consumerName")
      productBuilder <- ProducerBuilder.make(JSchema.STRING)
      producer       <- productBuilder
                          .topic(topic)
                          .loadConf(Producer.producerName("hello-producer"))
                          .build
      _              <- producer.send("Hello!")
      m              <- consumer.receive
      _              <- Console.printLine(m.getValue)
    yield ()

  override def run = app.provideLayer(pulsarClient ++ Scope.default).exitCode
