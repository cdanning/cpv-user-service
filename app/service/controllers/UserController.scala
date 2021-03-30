package service.controllers

import play.api.Configuration
import play.api.libs.json._
import play.api.libs.ws._
import play.api.mvc.{Action, _}
import play.utils.UriEncoding
import service.models.{Role, RoleMgmt, User}
import service.repo.UserRepo

import javax.inject._
import scala.collection.immutable.Seq
import scala.util.{Failure, Success, Try}

/**
  * This controller creates an `Action` to handle HTTP requests to the
  * application's home page.
  */
@Singleton
class UserController @Inject()(ws: WSClient, val controllerComponents: ControllerComponents,
                               configuration: Configuration
                              ) extends BaseController {

  val logService = configuration.get[String]("log.endpoint")
  
  implicit val formatUserDto: Format[UserDto] = Json.format
  implicit val formatUser: Format[User] = Json.format


  /**
    * @return a json list of all known users
    */
  def getUsers(): Action[AnyContent] = Action {
    if (UserRepo.userList.isEmpty) {
      NoContent
    } else {
      Ok(Json.toJson(UserRepo.userList.values))
    }
  }

  /**
    * create a new user (unique pseudonym)
    * @return json object of the user
    */
  def createUser(): Action[AnyContent] = Action { implicit request =>
    val content = request.body
    val jsonObject = content.asJson
    val userDto: Option[UserDto] =
      jsonObject.flatMap(Json.fromJson[UserDto](_).asOpt)
    userDto match {
      case Some(newItem) =>
        newUser(newItem.pseudonym, newItem.roles) match {
          case Success(u) => Created(Json.toJson(u))
          case Failure(f) => BadRequest(f.getMessage)
        }
      case None =>
        BadRequest
    }
  }

  def newUser(pseudo: String, roles: Seq[Role]): Try[Option[User]] = {
    Try {
      val user = User(pseudo, Role.permissions(roles))
      UserRepo.userList.updateWith(pseudo)({
        case Some(_) => throw new IllegalStateException("duplicate user")
        case None =>
          log(user, "new", roles.mkString(","))
          Some(user)
      })
    }
  }

  /**
    * grant a role to user
    * @param user to user the role is for
    * @param role to name of the role
    * @return json object of the user
    */
  def grantRole(user: User, role: String): Action[AnyContent] = Action { implicit request =>
    val updated = RoleMgmt.grant(user, Role.withName(role))
    log(updated, "grant", role)
    UserRepo.userList.update(updated.pseudonym, updated)
    Ok(Json.toJson(updated))
  }

  /**
    * revoke a role from a user
    * @param user to user the role should removed from
    * @param role to name of the role
    * @return json object of the user
    */
  def revokeRole(user: User, role: String): Action[AnyContent] = Action { implicit request =>
    RoleMgmt.revoke(user, Role.withName(role)) match {
      case Success(u) =>
        log(u, "revoke", role)
        UserRepo.userList.update(u.pseudonym, u)
        Ok(Json.toJson(u)
        )
      case Failure(f) => BadRequest(f.getMessage)
    }
  }

  /**
    * @param user the user to be deleted
    * @return  json object of the user
    */
  def deleteUser(user: User): Action[AnyContent] = Action { implicit request =>
    log(user, "delete", "")
    UserRepo.userList.remove(user.pseudonym)
    Ok(Json.toJson(user))
  }

  def log(user: User, event: String, data: String): Unit = {
    val usr = enc(user.pseudonym)
    val evt = enc(event)
    val dat = enc(data)
    ws.url(s"${logService}/$usr/$evt/$dat").withBody("").post("")
  }

  def enc(pathPart: String): String = {
    UriEncoding.encodePathSegment(pathPart, "utf-8")
  }
}
