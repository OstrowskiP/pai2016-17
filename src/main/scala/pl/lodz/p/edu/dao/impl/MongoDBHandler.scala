package pl.lodz.p.edu.dao.impl

import org.mongodb.scala.bson.{BsonDouble, BsonInt32, BsonObjectId, BsonString, BsonValue}
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

  import MongoDBHandler._
  val logger: Logger = LoggerFactory.getLogger(this.getClass)

  val client: MongoClient = MongoClient()
  val database: MongoDatabase = client.getDatabase(dbName)
  val collection: MongoCollection[Document] =
    database.getCollection(collectionName)

  override def delete(doc: Document): Observable[DeleteResult] =
    collection.deleteOne(equal("id", getBsonValue(doc.get("id"))))

  override def findById(id: Int): Observable[Document] =
    collection.find(equal("id", id))

  override def find(doc: Document): Observable[Document] =
    collection.find(equal("id", getBsonValue(doc.get("id"))))

  override def findAll(): Observable[Document] =
    collection.find()

  override def update(doc: Document): Observable[UpdateResult] =
    collection.replaceOne(and(equal("id", getBsonValue(doc.get("id"))), lt("version", getBsonValue(doc.get("version")))), doc)

  def getBsonValue(optBson: Option[BsonValue]) = {
    optBson match {
      case Some(bson: BsonString) => bson.getValue
      case Some(bson: BsonDouble) => bson.getValue
      case Some(bson: BsonObjectId) => bson.getValue
      case Some(bson: BsonInt32) => bson.getValue
      case _ => logger.error(Property("getBsonValueError"))
    }
  }

  override def synchronize(docs: Seq[Document]): String = {
    for (doc <- docs) {
      collection.findOneAndReplace(and(equal("id", getBsonValue(doc.get("id"))), lt("version", getBsonValue(doc.get("version")))), doc, FindOneAndReplaceOptions().upsert(true)).toFuture
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

  override def subscribeToResult(id: Int, finder: Int => Observable[Document]): Option[Document] = {
    val fut: Future[Seq[Document]] = finder(id).toFuture()
    Await.ready(fut, Duration(1000, MILLISECONDS))

    fut.value match {
      case Some(fromDB) => extractDoc[Document](fromDB)
      case _ =>
        logger.error(Property("documentNotFound") + id)
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
}

object MongoDBHandler {
  def extractDoc[A](maybeDoc: Try[Seq[A]]): Option[A] = {
    maybeDoc match {
      case Success(docList) =>
        if (docList.isEmpty) {
          None
        } else {
          val result = docList.head
          Some(result)
        }
      case Failure(_) =>
        None
    }
  }

  def extractDocs[A](maybeDoc: Try[Seq[A]]): Option[Seq[A]] = {
    maybeDoc match {
      case Success(docList) =>
        if (docList.isEmpty) {
          None
        } else {
          val result = docList
          Some(result)
        }
      case Failure(_) =>
        None
    }
  }
}
