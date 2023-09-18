package zio.pulsar

import scala.jdk.CollectionConverters.*

import zio.*

import org.apache.pulsar.client.api.{ PulsarClient as JPulsarClient, PulsarClientException }

trait PulsarClient:
  def client: IO[PulsarClientException, JPulsarClient]
end PulsarClient

object PulsarClient:

  def live(url: String, config: Map[String, Any] = Map.empty): URLayer[Scope, PulsarClient] =
    val builder = JPulsarClient.builder().serviceUrl(url).loadConf(config.asJava)

    val cl = new PulsarClient {
      val client = ZIO.attempt(builder.build).refineToOrDie[PulsarClientException]
    }

    ZLayer(ZIO.acquireRelease(ZIO.succeed(cl))(c => c.client.map(_.close()).orDie))
  end live

  def live(host: String, port: Int): URLayer[Scope, PulsarClient] =
    live(s"pulsar://$host:$port")

  def make: ZIO[PulsarClient, PulsarClientException, JPulsarClient] =
    ZIO.environmentWithZIO[PulsarClient](_.get.client)
