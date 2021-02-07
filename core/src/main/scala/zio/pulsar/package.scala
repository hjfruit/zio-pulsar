package zio

import org.apache.pulsar.client.api.{ PulsarClient => JPulsarClient, PulsarClientException }
import zio._ //{ ZIO, ZManaged }

package object pulsar {

  // type MyService = Has[MyService.Service]

  // object MyService {

  //   trait Service {
  //     def run: IO[Exception, Unit]
  //   }

  //   val live: ULayer[MyService] = ZLayer.succeed(new Service {
  //     def run = Task.succeed(())
  //   })

  //   def run: ZIO[MyService, Exception, Unit] =
  //     ZIO.accessM[MyService](_.get.run)
  // }
  type PulsarClient = Has[PulsarClient.Service]

  object PulsarClient {

    trait Service {
      def client: IO[PulsarClientException, JPulsarClient]
    }

    def live(host: String, port: Int): ULayer[PulsarClient] =
      ZLayer.fromManaged {
        val builder = JPulsarClient.builder().serviceUrl(s"pulsar://$host:$port")
  
        val cl = new Service {
          val client = ZIO.effect(builder.build).refineToOrDie[PulsarClientException]
        }
  
        ZManaged.make(ZIO.effectTotal(cl))(c => c.client.map(_.close()).orDie)
      }

    def make: ZIO[PulsarClient, PulsarClientException, JPulsarClient] =
      ZIO.accessM[PulsarClient](_.get.client)
  }
}
