package zio.pulsar.util

import org.apache.pulsar.client.api.{ PulsarClientException, Schema }
import zio.*
import zio.pulsar.*

/**
 * @author
 *   梦境迷离
 * @version 1.0,2023/2/15
 */
object PulsarClientF:

  def consumeF[K <: SubscriptionKind, M](
    topic: String,
    subscription: Subscription[K],
    schema: Schema[M]
  ): ZIO[PulsarClient & Scope, PulsarClientException, Consumer[M]] =
    for
      builder <- ConsumerBuilder.make(schema)
      cr      <- builder
                   .topic(topic)
                   .subscription(subscription)
                   .build
    yield cr

  def consumeF[K <: SubscriptionKind](
    topic: String,
    subscription: Subscription[K]
  ): ZIO[PulsarClient & Scope, PulsarClientException, Consumer[String]] =
    consumeF(topic, subscription, Schema.STRING)

  def productF(topic: String): ZIO[PulsarClient & Scope, PulsarClientException, Producer[String]] =
    productF(topic, Schema.STRING)

  def productF[M](topic: String, schema: Schema[M]): ZIO[PulsarClient & Scope, PulsarClientException, Producer[M]] =
    for
      builder <- ProducerBuilder.make(schema)
      pd      <- builder
                   .topic(topic)
                   .build
    yield pd
