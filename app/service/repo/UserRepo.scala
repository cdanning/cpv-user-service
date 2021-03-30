package service.repo

import service.models.User

import scala.collection.mutable.LinkedHashMap

object UserRepo {
  val userList = new LinkedHashMap[String, User]
}
