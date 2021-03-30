package bindings

import play.api.mvc.PathBindable
import service.models.User
import service.repo.UserRepo

object PathBinders {

  implicit object UserBinder extends PathBindable[User] {
    override def bind(key: String, value: String): Either[String, User] = {
      UserRepo.userList.get(value).toRight(s"User not found '${value}'")
    }

    override def unbind(key: String, user: User) = user.pseudonym
  }

}
