package pl.lodz.p.edu.service

import pl.lodz.p.edu.dao.impl.MongoDBHandler
import pl.lodz.p.edu.util.Property

trait DBConfig {
  val usersDbHandler = new MongoDBHandler(Property("databaseName"), Property("usersCollectionName"))
  val windowsDbHandler = new MongoDBHandler(Property("databaseName"), Property("windowsCollectionName"))
}
