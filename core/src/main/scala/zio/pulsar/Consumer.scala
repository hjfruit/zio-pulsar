package zio.pulsar

import java.util.concurrent.TimeUnit

import scala.jdk.CollectionConverters.*

import zio.*
//import zio.blocking._
import zio.stream.*

import org.apache.pulsar.client.api.{ Consumer as JConsumer, Message, MessageId, PulsarClientException }

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
    ZIO.attemptBlocking(consumer.receive(timeout, unit)).refineToOrDie[PulsarClientException]

  val receiveAsync: IO[PulsarClientException, Message[M]] =
    ZIO.fromCompletionStage(consumer.receiveAsync).refineToOrDie[PulsarClientException]

  val receiveStream: Stream[PulsarClientException, Message[M]] =
    ZStream.repeatZIO(ZIO.attemptBlocking(consumer.receive).refineToOrDie[PulsarClientException])

  val batchReceiveStream: Stream[PulsarClientException, Message[M]] =
    ZStream.fromIterable(consumer.batchReceive().asScala)

  val batchReceive: IO[PulsarClientException, List[Message[M]]] =
    ZIO.attempt(consumer.batchReceive().asScala.toList).refineToOrDie[PulsarClientException]
