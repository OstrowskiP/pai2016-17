package pl.lodz.p.edu.service

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import org.json4s.DefaultFormats
import org.json4s.native.Serialization.write
import pl.lodz.p.edu.model.{User, Window}
import spray.json._

trait JsonSupport extends SprayJsonSupport with DefaultJsonProtocol {
  val userFormat = jsonFormat7(User)
  val windowFormat = jsonFormat5(Window)

  def toJson[T <: AnyRef](cl: T): String = write(cl)(DefaultFormats)
}
