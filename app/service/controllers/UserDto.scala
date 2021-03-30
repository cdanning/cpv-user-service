package service.controllers

import service.models.Role

final case class UserDto(pseudonym: String, roles: Seq[Role])
