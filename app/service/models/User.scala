package service.models

case class User(pseudonym: String, role: Set[Role]) {
  require(role != null && Role.permissions(role).nonEmpty, "role is null or empty")
  require(pseudonym != null && !pseudonym.isBlank, "pseudonym is null or empty")
}


