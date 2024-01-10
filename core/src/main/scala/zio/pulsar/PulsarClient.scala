package zio.pulsar

import java.util.concurrent.TimeUnit

import scala.jdk.CollectionConverters.*

import zio.*

import org.apache.pulsar.client.api.{ PulsarClient as JPulsarClient, PulsarClientException }

trait PulsarClient:
  def client: IO[PulsarClientException, JPulsarClient]
end PulsarClient

object PulsarClient:

  def live(config: PulsarClientConfig): ZLayer[Scope, Throwable, PulsarClient] =
    val cl = for {
      _client <- ZIO.attempt {
                   val _client = JPulsarClient.builder().serviceUrl(config.serviceUrl)
                   config.loadConf.foreach(c => _client.loadConf(c.asJava))
                   config.serviceUrlProvider.foreach(_client.serviceUrlProvider)
                   config.connectionMaxIdleSeconds.foreach(_client.connectionMaxIdleSeconds)
                   config.authentication.foreach(_client.authentication)
                   config.operationTimeout.foreach(c => _client.operationTimeout(c.toSeconds.toInt, TimeUnit.SECONDS))
                   config.lookupTimeout.foreach(c => _client.lookupTimeout(c.toSeconds.toInt, TimeUnit.SECONDS))
                   config.ioThreads.foreach(_client.ioThreads)
                   config.listenerThreads.foreach(_client.listenerThreads)
                   config.connectionsPerBroker.foreach(_client.connectionsPerBroker)
                   config.maxConcurrentLookupRequests.foreach(_client.maxConcurrentLookupRequests)
                   config.statsInterval.foreach(c => _client.statsInterval(c.toMillis, TimeUnit.MILLISECONDS))
                   config.maxLookupRequests.foreach(_client.maxLookupRequests)
                   config.maxLookupRedirects.foreach(_client.maxLookupRedirects)
                   config.maxNumberOfRejectedRequestPerConnection.foreach(
                     _client.maxNumberOfRejectedRequestPerConnection
                   )
                   config.keepAliveInterval.foreach(c => _client.keepAliveInterval(c.toSeconds.toInt, TimeUnit.SECONDS))
                   config.connectionTimeout.foreach(c => _client.connectionTimeout(c.toSeconds.toInt, TimeUnit.SECONDS))
                   config.startingBackoffInterval.foreach(c =>
                     _client.startingBackoffInterval(c.toMillis, TimeUnit.MILLISECONDS)
                   )
                   config.maxBackoffInterval.foreach(c => _client.maxBackoffInterval(c.toMillis, TimeUnit.MILLISECONDS))
                   config.enableBusyWait.foreach(_client.enableBusyWait)
                   config.clock.foreach(_client.clock)
                   config.enableTransaction.foreach(_client.enableTransaction)
                   _client
                 }
    } yield new PulsarClient {
      val client: IO[PulsarClientException, JPulsarClient] = ZIO
        .attempt(_client.build)
        .refineToOrDie[PulsarClientException]
    }

    ZLayer
      .fromZIO(
        ZIO.acquireRelease(cl)(c => c.client.map(_.close()).ignoreLogged)
      )
  end live

  def live(host: String, port: Int): ZLayer[Scope, Throwable, PulsarClient] = {
    val conf = PulsarClientConfig.apply(serviceUrl = s"pulsar://$host:$port")
    live(conf)
  }

  def make: ZIO[PulsarClient, PulsarClientException, JPulsarClient] =
    ZIO.serviceWithZIO[PulsarClient](_.client)
