package zio.pulsar.json

import java.nio.charset.StandardCharsets

import scala.reflect.*

import zio.json.*

import org.apache.pulsar.client.api.Schema
import org.apache.pulsar.client.impl.schema.SchemaInfoImpl
import org.apache.pulsar.common.schema.{ SchemaInfo, SchemaType }

import com.sksamuel.avro4s.{ AvroSchema, SchemaFor }

object JsonSchema:

  def jsonSchema[T: ClassTag](using codec: JsonCodec[T], avroSchema: SchemaFor[T]): Schema[T] =
    new Schema[T] {

      override def clone(): Schema[T] = this

      override def encode(t: T): Array[Byte] = t.toJson.getBytes(StandardCharsets.UTF_8)

      override def decode(bytes: Array[Byte]): T =
        new String(bytes, StandardCharsets.UTF_8)
          .fromJson[T]
          .fold(s => throw new RuntimeException(s), identity)

      val s = AvroSchema[T](using avroSchema)

      override def getSchemaInfo: SchemaInfo =
        SchemaInfoImpl.builder
          .name(classTag[T].runtimeClass.getCanonicalName)
          .`type`(SchemaType.JSON)
          .schema(s.toString.getBytes(StandardCharsets.UTF_8))
          .build
    }
