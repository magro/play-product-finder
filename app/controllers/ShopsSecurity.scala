package controllers

import play.api.mvc._
import play.api.mvc.Security.AuthenticatedBuilder
import play.api.data._
import play.api.data.validation.Constraints._
import play.api.data.Forms._
import play.api.i18n.Messages

private[controllers] object ShopsSecurity {

  val Username: String = "username"
  val Password = "password"
  val OriginalUrl = "originalUrl"

  val ValidUsername = "play@example.com"
  val ValidPassword = "secret"

}

private[controllers] trait ShopsSecurity { self: Controller =>

  import ShopsSecurity._

  private[controllers] object Authenticated extends AuthenticatedBuilder(req => getUserFromRequest(req), onUnauthorized)

  private def getUserFromRequest(request: RequestHeader) = request.session.get(Username)

  /**
   * When a secured resource was accessed save the requested url in the session and redirect to login.
   */
  private def onUnauthorized: RequestHeader => SimpleResult = { rh: RequestHeader =>
    Redirect(routes.ShopsController.login).withSession(OriginalUrl -> rh.uri)
  }

  /**
   * Describe the login form, contains just username and password.
   */
  private val loginForm = Form(tuple(Username -> email.verifying(nonEmpty), Password -> nonEmptyText))

  /**
   * Show the login page.
   */
  def login = Action { implicit request =>
    Ok(views.html.login(loginForm))
  }

  /**
   * Handle login form submit: authenticates the user and either displays
   * the login page again (with errors) or redirect to the originally requested
   * url (or shop list when there's no original url saved in the session).
   */
  def authenticate = Action { implicit request =>
    loginForm.bindFromRequest.fold(
      // binding failure, retrieving the form containing errors:
      formWithErrors => {
        BadRequest(views.html.login(formWithErrors))
      },
      // binding success, getting the actual value
      loginData => loginData match {
        case (username, password) => {
          if (!ValidUsername.equals(username) || !ValidPassword.equals(password)) {
            val form = loginForm.bindFromRequest
            if (!ValidUsername.equals(username))
              BadRequest(views.html.login(form.withError(Username, "invalid")))
            else
              BadRequest(views.html.login(form.withError(Password, "invalid")))
          } else {
            request.session.get(OriginalUrl)
              .map(origUrl => Redirect(origUrl))
              .getOrElse(Redirect(routes.ShopsController.list()))
              .withSession(Username -> username).flashing("success" -> Messages("shops.login.success"))
          }
        }
      })
  }

  /**
   * Logout the user, redirect to login.
   */
  def logout = Action { implicit request =>
    Redirect(routes.ShopsController.login).withNewSession.flashing("success" -> "You've been logged out")
  }

  /**
   * Retrieves the username of the logged in user. Returns <code>None</code> if there's no user logged in.
   */
  def loggedInUser(implicit request: Request[_]): Option[String] = request match {
    case authReq: Security.AuthenticatedRequest[_, _] => Some(authReq.user.asInstanceOf[String])
    case _ => None
  }

}