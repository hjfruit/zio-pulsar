package zio.pulsar

import zio.*
import zio.json.*
import zio.pulsar.json.*
import zio.test.Assertion.{ assertion, equalTo }
import zio.test.junit.JUnitRunnableSpec
import zio.test.{ assertZIOImpl, suite, test }
import zio.test.TestAspect.sequential
import org.apache.pulsar.client.api.{
  BatchReceivePolicy,
  PulsarClientException,
  RegexSubscriptionMode,
  Schema as JSchema
}
import zio.test.Assertion.*
import zio.test.*

import java.time.LocalDate
import java.util.concurrent.TimeUnit

object PulsarClientSpec extends PulsarContainerSpec:

  case class Order(
    item: String,
    price: BigDecimal,
    quantity: Int,
    description: Option[String],
    comment: Option[String],
    date: LocalDate
  )

  def specLayered: Spec[PulsarEnvironment, PulsarClientException] = suite("PulsarClient")(
    test("send and receive String message") {
      val topic = "my-test-topic"
      for
        builder        <- ConsumerBuilder.make(JSchema.STRING)
        consumer       <- builder
                            .topic(topic)
                            .subscription(Subscription("my-test-subscription", SubscriptionType.Exclusive))
                            .build
        productBuilder <- ProducerBuilder.make(JSchema.STRING)
        producer       <- productBuilder.topic(topic).build
        _              <- producer.send("Hello!")
        m              <- consumer.receive
      yield assertTrue(m.getValue == "Hello!")
    },
    test("send and receive JSON message") {
      given jsonCodec: JsonCodec[Order] = DeriveJsonCodec.gen[Order]
      val topic                         = "my-test-topic-2"
      val message                       = Order("test item", 10.5, 5, Some("test description"), None, LocalDate.of(2000, 1, 1))
      for
        builder        <- ConsumerBuilder.make(Schema.jsonSchema[Order])
        consumer       <- builder
                            .topic(topic)
                            .subscription(Subscription("my-test-subscription-2", SubscriptionType.Exclusive))
                            .build
        productBuilder <- ProducerBuilder.make(Schema.jsonSchema[Order])
        producer       <- productBuilder.topic(topic).build
        _              <- producer.send(message)
        m              <- consumer.receive
      yield assertTrue(m.getValue == message)

    },
    test("send and batch receive JSON message") {
      given jsonCodec: JsonCodec[Order] = DeriveJsonCodec.gen[Order]

      val topic   = "my-test-topic-3"
      val message = Order("test item", 10.5, 5, Some("test description"), None, LocalDate.of(2000, 1, 1))
      for
        builder        <- ConsumerBuilder.make(Schema.jsonSchema[Order])
        consumer       <- builder
                            .topic(topic)
                            .batchReceivePolicy(
                              BatchReceivePolicy
                                .builder()
                                .maxNumMessages(10)
                                .maxNumBytes(1024)
                                .timeout(10, TimeUnit.SECONDS)
                                .build()
                            )
                            .subscription(Subscription("my-test-subscription-3", SubscriptionType.Exclusive))
                            .build
        productBuilder <- ProducerBuilder.make(Schema.jsonSchema[Order])
        producer       <- productBuilder.topic(topic).build
        _              <- ZIO.foreach(0 to 10)(_ => producer.send(message))
        ms             <- consumer.batchReceive
      yield assertTrue(ms.size == 10)
    },
    test("send and batch receive stream JSON message") {
      given jsonCodec: JsonCodec[Order] = DeriveJsonCodec.gen[Order]

      val topic   = "my-test-topic-4"
      val message = Order("test item", 10.5, 5, Some("test description"), None, LocalDate.of(2000, 1, 1))
      for
        builder        <- ConsumerBuilder.make(Schema.jsonSchema[Order])
        consumer       <- builder
                            .topic(topic)
                            .batchReceivePolicy(
                              BatchReceivePolicy
                                .builder()
                                .maxNumMessages(10)
                                .maxNumBytes(1024)
                                .timeout(10, TimeUnit.SECONDS)
                                .build()
                            )
                            .subscription(Subscription("my-test-subscription-4", SubscriptionType.Exclusive))
                            .build
        productBuilder <- ProducerBuilder.make(Schema.jsonSchema[Order])
        producer       <- productBuilder.topic(topic).build
        _              <- ZIO.foreach(0 to 10)(_ => producer.send(message))
        ms             <- consumer.batchReceiveStream.runCollect
      yield assertTrue(ms.size == 10)
    }
  ) @@ sequential
