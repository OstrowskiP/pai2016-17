package pl.lodz.p.edu.dao.impl

import org.mongodb.scala.bson.{BsonDouble, BsonObjectId, BsonString, BsonValue}
import org.mongodb.scala.model.Filters._
import org.mongodb.scala.model.FindOneAndReplaceOptions
import org.mongodb.scala.result.{DeleteResult, UpdateResult}
import org.mongodb.scala.{Completed, Document, MongoClient, MongoCollection, MongoDatabase, Observable}
import org.slf4j.{Logger, LoggerFactory}
import pl.lodz.p.edu.dao.GenDBHandler
import pl.lodz.p.edu.util._

import scala.concurrent.duration._
import scala.concurrent.{Await, Future}
import scala.util.{Failure, Success, Try}

class MongoDBHandler(dbName: String, collectionName: String)
  extends GenDBHandler {

  val logger: Logger = LoggerFactory.getLogger(this.getClass)

  val client: MongoClient = MongoClient()
  val database: MongoDatabase = client.getDatabase(dbName)
  val collection: MongoCollection[Document] =
    database.getCollection(collectionName)

  override def delete(doc: Document): Observable[DeleteResult] =
    collection.deleteOne(equal("id", getBsonValue(doc.get("id"))))

  def getBsonValue(optBson: Option[BsonValue]) = {
    optBson match {
      case Some(bson: BsonString) => bson.getValue
      case Some(bson: BsonDouble) => bson.getValue
      case Some(bson: BsonObjectId) => bson.getValue
      case _ => logger.error(Property("getBsonValueError"))
    }
  }

  override def find(doc: Document): Observable[Document] =
    collection.find(equal("id", getBsonValue(doc.get("id"))))

  override def findAll(): Observable[Document] =
    collection.find()

  override def update(doc: Document): Observable[UpdateResult] =
    collection.replaceOne(equal("id", getBsonValue(doc.get("id"))), doc)

  override def synchronize(docs: Seq[Document]): String = {
    for (doc <- docs) {
      val resp = collection.findOneAndReplace(equal("id", getBsonValue(doc.get("id"))), doc, FindOneAndReplaceOptions().upsert(true)).toFuture
    }
    "Synchronization completed."
  }

  override def create(doc: Document): Observable[Completed] =
    collection.insertOne(doc)

  override def subscribeToResult[A](doc: Document, finder: Document => Observable[A]): Option[A] = {
    val fut: Future[Seq[A]] = finder(doc).toFuture()
    Await.ready(fut, Duration(1000, MILLISECONDS))

    fut.value match {
      case Some(fromDB) => extractDoc[A](fromDB)
      case _ =>
        logger.error(Property("documentNotFound") + doc)
        None
    }
  }

  override def subscribeToResults[A](docs: Seq[Document], finder: Seq[Document] => Observable[A]): Option[A] = {
    val fut: Future[Seq[A]] = finder(docs).toFuture()
    Await.ready(fut, Duration(1000, MILLISECONDS))

    fut.value match {
      case Some(fromDB) => extractDoc[A](fromDB)
      case _ =>
        logger.error(Property("documentNotFound") + docs)
        None
    }
  }

  override def extractDoc[A](maybeDoc: Try[Seq[A]]): Option[A] = {
    maybeDoc match {
      case Success(docList) => {
        if (docList.isEmpty) {
          logger.info(Property("documentRetrievedButEmpty"))
          None
        } else {
          val result = docList.head
          logger.info(Property("documentRetrieved") + result)
          Some(result)
        }
      }
      case Failure(ex) => {
        logger.error(Property(ex.getMessage))
        None
      }
    }
  }
}
