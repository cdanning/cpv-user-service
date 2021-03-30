package service.models

import scala.util.{Failure, Success, Try}

object RoleMgmt {

  def grant(user: User, perm: Role): User = {
    val oldPerm = Role.permissions(user.role)
    val newPerm = Role.grant(user.role, perm)
    if (oldPerm == newPerm)
      user
    else
      User(user.pseudonym, newPerm)
  }

  def revoke(user: User, perm: Role): Try[User] = {
    val oldPerm = Role.permissions(user.role)
    val newPerm = Role.revoke(user.role, perm)
    if (oldPerm == newPerm)
      Success(user)
    else {
      newPerm match {
        case Nil => Failure(new IllegalStateException("no role for user"))
        case p => Success(User(user.pseudonym, p))
      }
    }
  }

}
