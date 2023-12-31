package zio.pulsar

import java.util

import scala.jdk.CollectionConverters.*

import zio.*
import zio.json.*
import zio.pulsar.admin.{ AdminConfigPart, PulsarAdminClient }
import zio.pulsar.admin.AdminConfigPart.ConfigComplete
import zio.pulsar.json.*
import zio.test.*
import zio.test.Assertion.*
import zio.test.TestAspect.sequential

import org.apache.pulsar.client.api.{ Authentication, EncodedAuthenticationParameterSupport }

import com.dimafeng.testcontainers.PulsarContainer

object PulsarAdminClientSpec extends ZIOSpecDefault:

  def makeContainer: ZIO[Any, Throwable, PulsarAdminClient[ConfigComplete]] =
    ZIO
      .serviceWithZIO[PulsarContainer](a => PulsarAdminClient.make(a.httpServiceUrl()))
      .provideLayer(Scope.default >>> TestContainer.pulsar)

  def makeContainer2: ZIO[Any, Throwable, PulsarAdminClient[ConfigComplete]] =
    ZIO
      .serviceWithZIO[PulsarContainer](a => PulsarAdminClient.make.map(_.serviceHttpUrl(a.httpServiceUrl())))
      .provideLayer(Scope.default >>> TestContainer.pulsar)

  final case class User(secret: String)
  given jsonCodec: JsonCodec[User] = DeriveJsonCodec.gen[User]

  override def spec =
    suite("PulsarAdmin")(
      test("auth with pwd") {
        for
          m1 <- makeContainer
                  .flatMap(
                    _.authentication(
                      classOf[MockAuthenticationSecret].getName,
                      new String(JsonSchema.jsonSchema[User].encode(User("apachepulsar")))
                    ).build
                  )
          m2 <- makeContainer2
                  .flatMap(
                    _.authentication(
                      classOf[MockAuthenticationSecret].getName,
                      new String(JsonSchema.jsonSchema[User].encode(User("apachepulsar")))
                    ).build
                  )
        yield assertTrue(m1.getServiceUrl != null) && assertTrue(m2.getServiceUrl != null)

      }
    ) @@ sequential

  class MockAuthenticationSecret extends Authentication with EncodedAuthenticationParameterSupport {
    private var secret: String = null

    def getAuthMethodName = "mock-secret"

    def configure(encodedAuthParamString: String): Unit =
      secret = JsonSchema
        .jsonSchema[Map[String, String]]
        .decode(encodedAuthParamString.getBytes)
        .getOrElse("secret", "secret")

    def start(): Unit = {}

    def close(): Unit = {}

    override def configure(authParams: util.Map[String, String]): Unit =
      configure(
        new String(
          JsonSchema.jsonSchema[Map[String, String]].encode(authParams.asScala.toMap)
        )
      )
  }

end PulsarAdminClientSpec
