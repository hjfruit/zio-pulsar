package zio.pulsar

import scala.jdk.CollectionConverters.*

import zio.pulsar.Consumer.*

private[pulsar] abstract class Properties private (
  private val propertyList: List[Property[_]] = Nil
):
  // format: off

  def getProperties: Map[String, String] = getConfig.map(f => f._1 -> f._2.toString)

  def getConfig: Map[String, Any] =
    this.propertyList
      .filter(_ != null)
      .foldLeft(List.empty[(String, Any)]) { (op, a) =>
        op.:: {
          a match
            case topicNames(value) => a.name -> value.toSet
            case properties(value) => a.name -> value.asJava
            case _                 => a.name -> a.value
        }
      }
      .toMap
end Properties

object Properties:

  private[pulsar] final case class ConsumerProperties(c: ConsumerProperty[_], cl: List[ConsumerProperty[_]]) extends Properties(c :: cl)
  private[pulsar] final case class ProducerProperties(p: ProducerProperty[_], pl: List[ProducerProperty[_]]) extends Properties(p :: pl)

  private[pulsar] final case class StringProperties(s: StringProperty, sl: List[StringProperty])
      extends Properties(s :: sl)

end Properties

trait Property[+T]:
  self =>
  def name: String = self.getClass.getSimpleName
  def value: T
end Property


final case class StringProperty(key: String, value: String) extends Property[String] {
  override def name: String = key
}
private [pulsar] trait ProducerProperty[+T] extends Property[T]
private [pulsar]trait ConsumerProperty[+T] extends Property[T]