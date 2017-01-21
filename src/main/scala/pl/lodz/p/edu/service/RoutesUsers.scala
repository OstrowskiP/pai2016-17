package pl.lodz.p.edu.service

import akka.http.scaladsl.model.{HttpEntity, HttpResponse, MediaTypes}
import akka.http.scaladsl.server.{Directives, Route}
import org.mongodb.scala.Document
import org.slf4j.{Logger, LoggerFactory}
import pl.lodz.p.edu.dao.UsersDao
import pl.lodz.p.edu.dao.impl.MongoDBHandler
import pl.lodz.p.edu.model.User

import scala.concurrent.Await
import scala.concurrent.duration.{Duration, MILLISECONDS}
import scala.util.parsing.json.JSONArray

trait RoutesUsers extends Directives with JsonSupport {
  implicit val formatUsers = userFormat
  val usersRepository: UsersDao
  val routesUsers: Route =
    pathPrefix("users") {
      path("synchronize" ~ Slash.?) {
        post {
          entity(as[Seq[User]]) { users =>
            val resp = usersRepository.synchronize(users)
            complete {
              logger.info("Succesfully synchronized users.")
              HttpResponse(entity = HttpEntity(encoding, resp.toString))
            }
          }
        } ~ get {
          val users = usersRepository.findAll().toFuture
          Await.ready(users, Duration(10000, MILLISECONDS))
          val docList = MongoDBHandler.extractDocs(users.value.get).get map (doc => Document(
            "id" -> doc.get("id").get,
            "login" -> doc.get("login").get,
            "passwordHash" -> doc.get("passwordHash").get,
            "firstName" -> doc.get("firstName").get,
            "lastName" -> doc.get("lastName").get,
            "accessLevel" -> doc.get("accessLevel").get,
            "active" -> doc.get("active").get,
            "version" -> doc.get("version").get
          ))
          val jsonList = docList map { doc =>
            doc.toJson
          } toList
          val jsonArrStr: String = JSONArray(jsonList).toString().replace("\\", "").replace("\"{", "{").replace("}\"", "}")
          complete {
            HttpResponse(entity = HttpEntity(encoding, jsonArrStr))
          }
        }
      } ~ path("update" ~ Slash.?) {
        post {
          entity(as[User]) { user =>
            val resp = usersRepository.update(user)
            complete {
              logger.info("Updated user: " + user)
              HttpResponse(entity = HttpEntity(encoding, resp.toString))
            }
          }
        }
      } ~ path("delete" ~ Slash.?) {
        post {
          entity(as[User]) { user =>
            val resp = usersRepository.delete(user)
            complete {
              if (resp)
                logger.info("Deleted user: " + user)
              else
                logger.info("There is no such a user.")
              HttpResponse(entity = HttpEntity(encoding, resp.toString))
            }
          }
        }
      }
    }
  private val logger: Logger = LoggerFactory.getLogger(this.getClass)
  private val encoding = MediaTypes.`application/json`
}
