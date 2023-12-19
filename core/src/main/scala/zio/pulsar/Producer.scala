package zio.pulsar

import zio.{ IO, Scope, ZIO }
import zio.pulsar.Consumer.*
import zio.stream.{ Sink, ZSink }

import org.apache.pulsar.client.api.{
  CompressionType,
  HashingScheme,
  MessageId,
  MessageRoutingMode,
  Producer as JProducer,
  ProducerCryptoFailureAction,
  PulsarClientException,
  Schema
}

final class Producer[M](val producer: JProducer[M]):

  def send(message: M): IO[PulsarClientException, MessageId] =
    ZIO.attempt(producer.send(message)).refineToOrDie[PulsarClientException]

  def sendAsync(message: M): IO[PulsarClientException, MessageId] =
    ZIO.fromCompletionStage(producer.sendAsync(message)).refineToOrDie[PulsarClientException]

  def asSink: Sink[PulsarClientException, M, M, Unit] = ZSink.foreach(m => send(m))

  def asSinkAsync: Sink[PulsarClientException, M, M, Unit] = ZSink.foreach(m => sendAsync(m))
end Producer

object Producer:

  def make(topic: String): ZIO[PulsarClient & Scope, PulsarClientException, Producer[Array[Byte]]] =
    val producer = PulsarClient.make.flatMap { client =>
      val builder = client.newProducer.topic(topic)
      ZIO.attempt(new Producer(builder.create)).refineToOrDie[PulsarClientException]
    }
    ZIO.acquireRelease(producer)(p => ZIO.attempt(p.producer.close()).orDie)

  def make[M](topic: String, schema: Schema[M]): ZIO[PulsarClient & Scope, PulsarClientException, Producer[M]] =
    val producer = PulsarClient.make.flatMap { client =>
      val builder = client.newProducer(schema).topic(topic)
      ZIO.attempt(new Producer(builder.create)).refineToOrDie[PulsarClientException]
    }
    ZIO.acquireRelease(producer)(p => ZIO.attempt(p.producer.close()).orDie)
  end make

  // see https://pulsar.apache.org/docs/2.10.x/client-libraries-java/#configure-producer
  final case class topicName[T <: String](value: T) extends ProducerProperty[T]

  final case class producerName[T <: String](value: T) extends ProducerProperty[T]

  final case class sendTimeoutMs[T <: Long](value: T) extends ProducerProperty[T]

  final case class blockIfQueueFull[T <: Boolean](value: T) extends ProducerProperty[T]

  final case class maxPendingMessages[T <: Int](value: T) extends ProducerProperty[T]

  final case class maxPendingMessagesAcrossPartitions[T <: Int](value: T) extends ProducerProperty[T]

  final case class messageRoutingMode[T <: MessageRoutingMode](value: T) extends ProducerProperty[T]

  final case class hashingScheme[T <: HashingScheme](value: T) extends ProducerProperty[T]

  final case class cryptoFailureAction[T <: ProducerCryptoFailureAction](value: T) extends ProducerProperty[T]

  final case class batchingMaxPublishDelayMicros[T <: Long](value: T) extends ProducerProperty[T]

  final case class batchingMaxMessages[T <: Int](value: T) extends ProducerProperty[T]

  final case class batchingEnabled[T <: Boolean](value: T) extends ProducerProperty[T]

  final case class chunkingEnabled[T <: Boolean](value: T) extends ProducerProperty[T]

  final case class compressionType[T <: CompressionType](value: T) extends ProducerProperty[T]

  final case class initialSubscriptionName[T <: String](value: T) extends ProducerProperty[T]
