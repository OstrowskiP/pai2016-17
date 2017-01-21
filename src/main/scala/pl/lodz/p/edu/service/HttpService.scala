package pl.lodz.p.edu.service

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.stream.ActorMaterializer
import org.slf4j.{Logger, LoggerFactory}
import pl.lodz.p.edu.dao.impl.{MongoDBHandler, MongoDBUsersDao, MongoDBWindowsDao}
import pl.lodz.p.edu.util.{Property, SimpleHttpClient}

import scala.concurrent.duration._
import scala.concurrent.{Await, ExecutionContext}
import scala.util.parsing.json.JSONArray

class HttpService extends Routes with RoutesWindows with DBConfig {
  val logger: Logger = LoggerFactory.getLogger(this.getClass)

  val usersRepository = new MongoDBUsersDao(usersDbHandler)
  val windowsRepository = new MongoDBWindowsDao(windowsDbHandler)

  implicit val system = ActorSystem()
  implicit val executor: ExecutionContext = system.dispatcher
  implicit val materializer: ActorMaterializer = ActorMaterializer()

  val port = Property.getInt("httpPort")

  def run() = {
    val actorSystem = ActorSystem()
    val scheduler = actorSystem.scheduler
    val task = new Runnable {
      def run() {
        logger.info("Invoking users synchronization with all external systems.")
        val users = usersRepository.findAll().toFuture
        Await.ready(users, Duration(2000, MILLISECONDS))
        val docList = MongoDBHandler.extractDoc(users.value.get).toList
        val jsonList = docList map { doc =>
          doc.toJson
        }
        val jsonArrStr = JSONArray(jsonList).toString()
        val client = new SimpleHttpClient(Property("externalServerHttpInterface"), Property.getInt("externalServerHttpPort"))
        val responseStatus = client.post(jsonArrStr, "api/admin/synchronise-listener")
        logger.info(s"Synchronization result: $responseStatus")
      }
    }
    implicit val executor = actorSystem.dispatcher

    scheduler.schedule(
      initialDelay = 10 seconds,
      interval = 10 seconds,
      runnable = task)

    Http().bindAndHandle(handler = routes,
      interface = Property("httpInterface"),
      port = port)

    logger.info(Property("serverName") + " - " + Property("serverWelcome") + port)
  }
}
