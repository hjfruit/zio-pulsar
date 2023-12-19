package zio.pulsar

import java.time.Clock

import scala.concurrent.duration.*

import org.apache.pulsar.client.api.{ Authentication, ServiceUrlProvider }

final case class PulsarClientConfig(
  serviceUrl: String,
  loadConf: Option[Map[String, Any]] = None,
  serviceUrlProvider: Option[ServiceUrlProvider] = None,
  connectionMaxIdleSeconds: Option[Int] = None,
  authentication: Option[Authentication] = None,
  operationTimeout: Option[Duration] = None,
  lookupTimeout: Option[Duration] = None,
  ioThreads: Option[Int] = None,
  listenerThreads: Option[Int] = None,
  connectionsPerBroker: Option[Int] = None,
  maxConcurrentLookupRequests: Option[Int] = None,
  statsInterval: Option[Duration] = None,
  maxLookupRequests: Option[Int] = None,
  maxLookupRedirects: Option[Int] = None,
  maxNumberOfRejectedRequestPerConnection: Option[Int] = None,
  keepAliveInterval: Option[Duration] = None,
  connectionTimeout: Option[Duration] = None,
  startingBackoffInterval: Option[Duration] = None,
  maxBackoffInterval: Option[Duration] = None,
  enableBusyWait: Option[Boolean] = None,
  clock: Option[Clock] = None,
  enableTransaction: Option[Boolean] = None
)
