package examples

import java.io.IOException

import zio.*
import zio.json.DeriveJsonCodec
import zio.json.JsonCodec
import zio.pulsar.*
import zio.pulsar.json.*

import org.apache.pulsar.client.api.{ PulsarClientException, RegexSubscriptionMode, Schema as JSchema }

import com.sksamuel.avro4s.{ AvroSchema, SchemaFor }

import RegexSubscriptionMode.*

case class User(email: String, name: Option[String], age: Int)

object SchemaExample extends ZIOAppDefault:

  val pulsarClient = PulsarClient.live("localhost", 6650)

  val topic = "my-schema-example-topic"

  given jsonCodec: JsonCodec[User] = DeriveJsonCodec.gen[User]

  val app: ZIO[PulsarClient with Scope, IOException, Unit] =
    for
      builder        <- ConsumerBuilder.make(JsonSchema.jsonSchema[User])
      consumer       <- builder
                          .topic(topic)
                          .subscription(Subscription("my-schema-example-subscription", SubscriptionType.Shared))
                          .build
      productBuilder <- ProducerBuilder.make(JsonSchema.jsonSchema[User])
      producer       <- productBuilder.topic(topic).build
      _              <- producer.send(User("test@test.com", None, 25))
      m              <- consumer.receive
      _              <- Console.printLine(m.getValue)
    yield ()

  override def run = app.provideLayer(pulsarClient ++ Scope.default).exitCode
