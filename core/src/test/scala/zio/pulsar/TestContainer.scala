package zio.pulsar

import zio._

import org.testcontainers.utility.DockerImageName

import com.dimafeng.testcontainers.PulsarContainer
import com.dimafeng.testcontainers.SingleContainer

object TestContainer {

  lazy val pulsar: ZLayer[Scope, Throwable, PulsarContainer] =
    ZLayer(ZIO.acquireRelease {
      val c = new PulsarContainer("2.11.0")
      ZIO.attempt(c.start()).as(c)
    }(container => ZIO.succeed(container.stop())))

}
