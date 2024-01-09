package zio.pulsar

import zio.*
import zio.ZLayer

import org.apache.pulsar.client.api.{
  CompressionType,
  MessageRoutingMode,
  ProducerAccessMode,
  ProducerBuilder as JProducerBuilder,
  PulsarClientException,
  Schema
}

/**
 * @author
 *   梦境迷离
 * @version 1.0,2024/1/9
 */
final class ZioPulsar(pulsarClient: PulsarClient) {

  def consumerBuilder[M](
    schema: Schema[M]
  ): ZIO[Any, PulsarClientException, ConsumerBuilder[M, ConsumerConfigPart.Empty, Nothing, Nothing]] =
    pulsarClient.client.map(c => new ConsumerBuilder(c.newConsumer(schema)))

  def producerBuilder[M](
    schema: Schema[M]
  ): ZIO[Any, PulsarClientException, ProducerBuilder[M, ProducerConfigPart.Empty]] =
    pulsarClient.client.map(c => new ProducerBuilder(c.newProducer(schema)))
}

object ZioPulsar {

  val live: ZLayer[PulsarClient, Throwable, ZioPulsar] =
    ZLayer.fromFunction((c: PulsarClient) => new ZioPulsar(c))
}
