package zio.pulsar

import org.apache.pulsar.client.api.{
  Message,
  Consumer => JConsumer,
  ConsumerBuilder,
  PulsarClient => JPulsarClient,
  PulsarClientException,
  SubscriptionType => JSubscriptionType
}
import zio.{ RIO, ZIO, ZManaged }
import zio.blocking.Blocking
import zio.stream._
import zio.pulsar.SubscriptionProperties._

import scala.jdk.CollectionConverters._

trait Consumer

object Consumer {

  final class Simple private[Consumer] (val consumer: JConsumer[Array[Byte]]) extends Consumer {

    val receive: RIO[Blocking, Message[Array[Byte]]] =
      ZIO.fromFutureJava(consumer.receiveAsync)

  }

  final class Streaming private[Consumer] (val consumer: JConsumer[Array[Byte]])
      extends Consumer {

    val receive: Stream[Throwable, Message[Array[Byte]]] = 
      ZStream.fromEffect(ZIO.fromCompletionStage(consumer.receiveAsync))

  }

  private def subscriptionType(t: SubscriptionType, builder: ConsumerBuilder[Array[Byte]]): ConsumerBuilder[Array[Byte]] =
    t match {
      case SubscriptionType.Exclusive(r) => 
        builder
          .subscriptionType(JSubscriptionType.Exclusive)
          .readCompacted(r)

      case SubscriptionType.Failover(r)  => 
        builder
          .subscriptionType(JSubscriptionType.Failover)
          .readCompacted(r)

      case SubscriptionType.Shared       => 
        builder
          .subscriptionType(JSubscriptionType.Shared)
          .readCompacted(false)

      case SubscriptionType.KeyShared(p) => 
        builder
          .subscriptionType(JSubscriptionType.Key_Shared)
          .readCompacted(false)
          .keySharedPolicy(p)
    }

  private def consumerBuilder(client: JPulsarClient, subscription: Subscription) = {
    val consumer = subscriptionType(subscription.`type`, client.newConsumer)
      .subscriptionName(subscription.name)
      .subscriptionInitialPosition(subscription.initialPosition)

    subscription.properties match {
      case TopicSubscriptionProperties(topics, mode) =>
        consumer
          .topics(topics.asJava)
          .subscriptionMode(mode)
      case PatternSubscriptionProperties(pattern, mode, period) =>
        consumer
          .topicsPattern(pattern)
          .subscriptionTopicsMode(mode)
          .patternAutoDiscoveryPeriod(period)
          
    }
  }

  def subscribe(subscription: Subscription): ZManaged[PulsarClient, PulsarClientException, Simple] = {
    val consumer = PulsarClient.client.map { client =>
      val builder = consumerBuilder(client, subscription)
      new Simple(builder.subscribe)
    }
    ZManaged.make(consumer)(p => ZIO.effect(p.consumer.close).orDie)
  }

  def streaming(subscription: Subscription): ZManaged[PulsarClient, PulsarClientException, Streaming] = {
    val consumer = for {
      client <- PulsarClient.client
      builder = consumerBuilder(client, subscription)
    } yield new Streaming(builder.subscribe)
    ZManaged.make(consumer)(p => ZIO.effect(p.consumer.close).orDie)
  }

}
