package pl.lodz.p.edu.dao.impl

import org.mongodb.scala.Completed
import org.mongodb.scala.bson.collection.immutable.Document
import org.mongodb.scala.bson.{BsonInt32, BsonString}
import pl.lodz.p.edu.dao.{GenDBHandler, WindowsDao}
import pl.lodz.p.edu.model.Window

import scala.concurrent.Await
import scala.concurrent.duration.{Duration, MILLISECONDS}

class MongoDBWindowsDao(dbHandler: GenDBHandler) extends WindowsDao {
  override def create(window: Window): String =
    dbHandler.subscribeToResult[Completed](createDocument(window), dbHandler.create).toString

  private def createDocument(window: Window): Document = {
    Document(
      "id" -> window.id,
      "name" -> window.name,
      "size" -> window.size,
      "color" -> window.color,
      "amount" -> window.amount
    )
  }

  override def findByName(window: Window): Window = {
    val resp = dbHandler.find(createDocument(window)).toFuture
    Await.ready(resp, Duration(1000, MILLISECONDS))

    val doc = resp.value match {
      case Some(fromDB) => dbHandler.extractDoc[Document](fromDB).get
      case _ => Document()
    }

    (for {
      id <- doc.get[BsonString]("id")
      name <- doc.get[BsonString]("name")
      size <- doc.get[BsonString]("size")
      color <- doc.get[BsonString]("color")
      amount <- doc.get[BsonInt32]("amount")
    } yield Window(id.getValue, name.getValue, size.getValue, color.getValue, amount.getValue)).get
  }

  override def update(window: Window): Boolean =
    dbHandler.subscribeToResult(createDocument(window), dbHandler.update).get.wasAcknowledged
}
