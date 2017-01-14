package pl.lodz.p.edu.service

import akka.http.scaladsl.model.{HttpEntity, HttpResponse, MediaTypes}
import akka.http.scaladsl.server.{Directives, Route}
import org.slf4j.{Logger, LoggerFactory}
import pl.lodz.p.edu.dao.UsersDao
import pl.lodz.p.edu.model.User

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
