package zio.pulsar

import zio.{ Scope, ZLayer }
import zio.test.*

import org.apache.pulsar.client.api.PulsarClientException

trait PulsarContainerSpec extends ZIOSpecDefault {

  override def spec =
    specLayered.provideLayerShared(
      ZLayer.make[PulsarEnvironment](
        Scope.default,
        testEnvironment,
        TestContainer.pulsar
          .flatMap(a => PulsarClient.live(a.get.pulsarBrokerUrl()))
          .orDie
      )
    )

  def specLayered: Spec[PulsarEnvironment, PulsarClientException]

}
