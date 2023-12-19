package zio.pulsar

import java.util.concurrent.TimeUnit
import java.util.regex.Pattern

import scala.collection.immutable.SortedMap
import scala.jdk.CollectionConverters.*

import zio.*
//import zio.blocking._
import zio.stream.*

import org.apache.pulsar.client.api.{ Consumer as JConsumer, Message, MessageId, PulsarClientException }
import org.apache.pulsar.client.api.{
  ConsumerCryptoFailureAction,
  DeadLetterPolicy,
  RegexSubscriptionMode,
  SubscriptionInitialPosition
}

final class Consumer[M](val consumer: JConsumer[M]):

  def acknowledge(messageId: MessageId): IO[PulsarClientException, Unit] =
    ZIO.attempt(consumer.acknowledge(messageId)).refineToOrDie[PulsarClientException]

  def acknowledge(message: Message[M]): IO[PulsarClientException, Unit] =
    ZIO.attempt(consumer.acknowledge(message)).refineToOrDie[PulsarClientException]

  def acknowledge(messages: Seq[MessageId]): IO[PulsarClientException, Unit] =
    ZIO.attempt(consumer.acknowledge(messages.asJava)).refineToOrDie[PulsarClientException]

  def negativeAcknowledge(messageId: MessageId): IO[PulsarClientException, Unit] =
    ZIO.attempt(consumer.negativeAcknowledge(messageId)).refineToOrDie[PulsarClientException]

  val receive: IO[PulsarClientException, Message[M]] =
    ZIO.attempt(consumer.receive).refineToOrDie[PulsarClientException]

  def receive(timeout: Int, unit: TimeUnit): IO[PulsarClientException, Message[M]] =
    ZIO.attempt(consumer.receive(timeout, unit)).refineToOrDie[PulsarClientException]

  val receiveAsync: IO[PulsarClientException, Message[M]] =
    ZIO.fromCompletionStage(consumer.receiveAsync).refineToOrDie[PulsarClientException]

  val receiveStream: Stream[PulsarClientException, Message[M]] =
    ZStream.repeatZIO(ZIO.attemptBlocking(consumer.receive).refineToOrDie[PulsarClientException])

  val batchReceiveStream: Stream[PulsarClientException, Message[M]] =
    ZStream.fromIterable(consumer.batchReceive().asScala)

  val batchReceive: IO[PulsarClientException, List[Message[M]]] =
    ZIO.attempt(consumer.batchReceive().asScala.toList).refineToOrDie[PulsarClientException]

end Consumer

// see https://pulsar.apache.org/docs/2.10.x/client-libraries-java/#configure-consumer
object Consumer:
  final case class topicNames[T <: String](value: List[T]) extends ConsumerProperty[List[T]]

  final case class topicsPattern[T <: Pattern](value: T) extends ConsumerProperty[T]

  final case class subscriptionName[T <: String](value: T) extends ConsumerProperty[T]

  final case class subscriptionType[K <: SubscriptionKind](value: SubscriptionType[K])
      extends ConsumerProperty[SubscriptionType[K]]

  final case class receiverQueueSize[T <: Int](value: T) extends ConsumerProperty[T]

  final case class acknowledgementsGroupTimeMicros[T <: Long](value: T) extends ConsumerProperty[T]

  final case class negativeAckRedeliveryDelayMicros[T <: Long](value: T) extends ConsumerProperty[T]

  final case class maxTotalReceiverQueueSizeAcrossPartitions[T <: Int](value: T) extends ConsumerProperty[T]

  final case class consumerName[T <: String](value: T) extends ConsumerProperty[T]

  final case class ackTimeoutMillis[T <: Long](value: T) extends ConsumerProperty[T]

  final case class tickDurationMillis[T <: Long](value: T) extends ConsumerProperty[T]

  final case class priorityLevel[T <: Int](value: T) extends ConsumerProperty[T]

  final case class cryptoFailureAction[T <: ConsumerCryptoFailureAction](value: T) extends ConsumerProperty[T]

  final case class properties[K <: String, V <: String](value: SortedMap[K, V])
      extends ConsumerProperty[SortedMap[K, V]]

  final case class readCompacted[T <: Boolean](value: T) extends ConsumerProperty[T]

  final case class subscriptionInitialPosition[T <: SubscriptionInitialPosition](value: T) extends ConsumerProperty[T]

  final case class patternAutoDiscoveryPeriod[T <: Int](value: T) extends ConsumerProperty[T]

  final case class regexSubscriptionMode[T <: RegexSubscriptionMode](value: T) extends ConsumerProperty[T]

  final case class deadLetterPolicy[T <: DeadLetterPolicy](value: T) extends ConsumerProperty[T]

  final case class autoUpdatePartitions[T <: Boolean](value: T) extends ConsumerProperty[T]

  final case class replicateSubscriptionState[T <: Boolean](value: T) extends ConsumerProperty[T]

  //    final case class negativeAckRedeliveryBackoff[T <: RedeliveryBackoff](value: T) extends ConsumerProperty[T]

  //    final case class ackTimeoutRedeliveryBackoff[T <: RedeliveryBackoff](value: T) extends ConsumerProperty[T]

  final case class autoAckOldestChunkedMessageOnQueueFull[T <: Boolean](value: T) extends ConsumerProperty[T]

  final case class maxPendingChunkedMessage[T <: Int](value: T) extends ConsumerProperty[T]

  final case class expireTimeOfIncompleteChunkedMessageMillis[T <: Long](value: T) extends ConsumerProperty[T]
end Consumer
