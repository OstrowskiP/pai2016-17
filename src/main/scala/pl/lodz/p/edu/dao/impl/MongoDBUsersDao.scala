package pl.lodz.p.edu.dao.impl

import org.mongodb.scala.result.{DeleteResult, UpdateResult}
import org.mongodb.scala.{Document, Observable}
import pl.lodz.p.edu.dao.{GenDBHandler, UsersDao}
import pl.lodz.p.edu.model.User

class MongoDBUsersDao(dbHandler: GenDBHandler) extends UsersDao {
  override def synchronize(users: Seq[User]): String = {
    val usersDocs = users map (user => createDocument(user))
    dbHandler.synchronize(usersDocs)
  }

  override def update(user: User): Boolean =
    dbHandler.subscribeToResult[UpdateResult](createDocument(user), dbHandler.update).get.getModifiedCount == 1


  override def delete(user: User): Boolean =
    dbHandler.subscribeToResult[DeleteResult](createDocument(user), dbHandler.delete).get.getDeletedCount == 1

  override def findAll(): Observable[Document] =
    dbHandler.findAll

  private def createDocument(user: User): Document = {
    Document(
      "id" -> user.id,
      "login" -> user.login,
      "passwordHash" -> user.passwordHash,
      "firstName" -> user.firstName,
      "lastName" -> user.lastName,
      "accessLevel" -> user.accessLevel,
      "active" -> user.active,
      "version" -> user.version
    )
  }
}
