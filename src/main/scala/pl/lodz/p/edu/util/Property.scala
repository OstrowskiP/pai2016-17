package pl.lodz.p.edu.util

import com.typesafe.config.{Config, ConfigFactory}

object Property {
  val dbProperties: Config = ConfigFactory.load("properties")

  def apply(property: String): String = {
    dbProperties.getString(property)
  }

  def getInt(property: String): Int = {
    dbProperties.getInt(property)
  }
}
