import java.io.IOException
import java.util.concurrent.TimeUnit

import zio.*
import zio.pulsar.*

import org.apache.pulsar.client.api.{ Message, MessageId, PulsarClientException, Schema as JSchema }

object UserService {
  val live = ZLayer.fromFunction(UserService.apply)
}

final case class UserService(zioPulsar: ZioPulsar) {

  val topic = "zio-topic"

  def sendPulsarNotCloseConsumer(): ZIO[Any, PulsarClientException, Message[String]] =
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
          .unsafeBuild
      )
      .flatMap(_.receive(10, TimeUnit.SECONDS))

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

object ZioPulsarExample extends ZIOAppDefault:

  val pulsarClient = PulsarClient.live("localhost", 6650)

  val topic = "single-topic"

  val app: ZIO[UserService with Scope, IOException, Unit] =
    (ZIO.serviceWithZIO[UserService](_.sendPulsar()) *> ZIO.serviceWithZIO[UserService](
      _.sendPulsarNotCloseConsumer()
    )).unit

  override def run = app.provide(UserService.live, ZioPulsar.live, Scope.default, pulsarClient).exitCode
