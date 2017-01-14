package pl.lodz.p.edu.model

case class User(id: Int, login: String, passwordHash: String, firstName: String, lastName: String, accessLevel: String, isActive: Boolean, version: Int)
