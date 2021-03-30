package service.models

import enumeratum.{Enum, EnumEntry, PlayJsonEnum}

import scala.collection.immutable.Iterable

sealed trait Role extends EnumEntry with Product with Serializable

object Role extends Enum[Role] with PlayJsonEnum[Role] {
  val values = findValues
  
  def revoke(roles: Set[Role], revokeRole: Role) = {
    (permissions _ andThen permissions _) (roles - revokeRole)
  }

  def grant(roles: Set[Role], grantRole: Role) = {
    (permissions _ andThen permissions _) (roles + grantRole)
  }

  /**
    * effective permissions of the give roles
    * @param roles
    * @return transitive roles of the given roles
    */
  def permissions(roles: Iterable[Role]) = {
    roles.flatMap(
      r => r match {
        case `admin` => Set(admin)
        case `read` => Set(read)
        case `write` => Set(read, write)
        case `signatory` => Set(read, write, signatory)
      }
    ).toSet

  }

  final case object admin extends Role

  final case object read extends Role

  final case object write extends Role

  final case object signatory extends Role
}
