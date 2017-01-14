package pl.lodz.p.edu.dao

import org.mongodb.scala.result.{DeleteResult, UpdateResult}
import org.mongodb.scala.{Completed, Document, MongoClient, MongoCollection, MongoDatabase, Observable}

import scala.util.Try

trait GenDBHandler {
  def client: MongoClient

  def database: MongoDatabase

  def collection: MongoCollection[Document]

  def delete(doc: Document): Observable[DeleteResult]

  def find(doc: Document): Observable[Document]

  def findAll(): Observable[Document]

  def update(doc: Document): Observable[UpdateResult]

  def synchronize(docs: Seq[Document]): String

  def create(doc: Document): Observable[Completed]

  def subscribeToResult[A](doc: Document, finder: Document => Observable[A]): Option[A]

  def subscribeToResults[A](docs: Seq[Document], finder: Seq[Document] => Observable[A]): Option[A]

  def extractDoc[A](maybeDoc: Try[Seq[A]]): Option[A]
}
