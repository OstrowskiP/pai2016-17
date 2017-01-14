package pl.lodz.p.edu.service

import akka.http.scaladsl.model.{HttpEntity, HttpResponse, MediaTypes}
import akka.http.scaladsl.server.{Directives, Route}
import org.slf4j.{Logger, LoggerFactory}
import pl.lodz.p.edu.dao.WindowsDao
import pl.lodz.p.edu.model.Window

trait RoutesWindows extends Directives with JsonSupport {
  implicit val formatWindows = windowFormat
  val windowsRepository: WindowsDao
  val routesWindows: Route =
    pathPrefix("windows") {
      path("add" ~ Slash.?) {
        post {
          entity(as[Window]) { window =>
            val resp = windowsRepository.create(window)
            complete {
              logger.info("Added window: " + window)
              HttpResponse(entity = HttpEntity(encoding, toJson(resp)))
            }
          }
        }
      } ~ path("update" ~ Slash.?) {
        post {
          entity(as[Window]) { window =>
            val resp = windowsRepository.update(window)
            complete {
              logger.info("Updated window: " + window)
              HttpResponse(entity = HttpEntity(encoding, resp.toString))
            }
          }
        }
      } ~ path("buy" ~ Slash.?) {
        post {
          entity(as[Window]) { window =>
            val resp: Window = windowsRepository.findByName(window)

            if (resp.amount > 0) {
              windowsRepository.update(resp.copy(amount = resp.amount - 1))
              logger.info("Bought window: " + window)
              complete("{\"available\":\"true\"}")
            } else {
              logger.info("Failed to buy window, insufficient amount.")
              complete("{\"available\":\"false\"}")
            }
          }
        }
      }
    }
  private val logger: Logger = LoggerFactory.getLogger(this.getClass)
  private val encoding = MediaTypes.`application/json`

}
