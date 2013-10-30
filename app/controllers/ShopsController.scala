package controllers

import java.nio.charset.Charset
import java.nio.charset.UnsupportedCharsetException
import scala.concurrent.Future
import models._
import models.activate._
import models.activate.shopPersistenceContext._
import net.fwbrasil.activate.ActivateContext
import net.fwbrasil.activate.play.EntityForm
import net.fwbrasil.activate.play.EntityForm._
import play.api._
import play.api.data._
import play.api.data.Forms._
import play.api.mvc._
import views._
import play.api.http.Status

object ShopsController extends Controller with ShopsSecurity {
  
  private val playThemeCookie = "play_theme"
  val defaultTheme = "default"

  /**
   * This result directly redirect to the application home.
   */
  private val Home = Redirect(routes.ShopsController.list(0, 2, ""))

  /**
   * Describe the shop form (used in both edit and create screens).
   */
  private val shopForm =
    EntityForm[Shop](
      _.name -> nonEmptyText,
      _.shortName -> optional(nonEmptyText),
      _.url -> nonEmptyText,
      _.active -> boolean,
      _.queryUrlTemplate -> nonEmptyText,
      _.queryUrlEncoding -> optional(nonEmptyText.verifying("error.invalidCharset", isCharset(_))),
      _.responseEncoding -> optional(nonEmptyText.verifying("error.invalidCharset", isCharset(_))),
      _.imageUrlBase -> optional(nonEmptyText),
      _.itemXPath -> nonEmptyText,
      _.nameXPath -> nonEmptyText,
      _.priceXPath -> nonEmptyText,
      _.imageUrlXPath -> nonEmptyText,
      _.detailsUrlXPath -> nonEmptyText)

  private def isCharset(value: String): Boolean = {
    try {
      Charset.forName(value)
      true
    } catch {
      case e: UnsupportedCharsetException => false
    }
  }

  // -- Actions

  /**
   * Handle default path requests, redirect to list
   */
  def index = Action { Home }

  /**
   * Display the paginated list of shops.
   *
   * @param page Current page number (starts from 0)
   * @param orderBy Column to be sorted
   * @param filter Filter applied on shop names
   */
  def list(page: Int, orderBy: Int, filter: String) = Authenticated.async { implicit request =>
    asyncTransactionalChain { implicit ctx =>
      Shop.list(page = page, orderBy = orderBy, filter = ("*" + filter + "*")).map {
        page => Ok(html.shopList(page, orderBy, filter))
      }
    }
  }

  /**
   * Display the 'new shop form'.
   */
  def create = Authenticated.async { implicit request =>
    asyncTransactionalChain { implicit ctx =>
      Future.successful(Ok(html.createForm(shopForm)))
    }
  }

  /**
   * Handle the 'new shop form' submission.
   */
  def save = Authenticated.async { implicit request =>
    asyncTransactionalChain { implicit ctx =>
      shopForm.bindFromRequest.fold(
        formWithErrors =>
          Future.successful(BadRequest(html.createForm(formWithErrors))),
        shopData => {
          shopData.asyncCreateEntity.map { shop =>
            Home.flashing("success" -> "Shop %s has been created".format(shop.name))
          }
        })
    }
  }

  /**
   * Display the 'edit form' of a existing Computer.
   *
   * @param id Id of the shop to edit
   */
  def edit(id: String) = Authenticated.async { implicit request =>
    asyncTransactionalChain { implicit ctx =>
      asyncById[Shop](id).map { shopOption =>
        shopOption.map { shop =>
          Ok(html.editForm(id, shopForm.fillWith(shop)))
        }.getOrElse(NotFound)
      }
    }
  }

  /**
   * Handle the 'edit form' submission
   *
   * @param id Id of the shop to edit
   */
  def update(id: String) = Authenticated.async { implicit request =>
    asyncTransactionalChain { implicit ctx =>
      shopForm.bindFromRequest.fold(
        formWithErrors =>
          Future.successful(BadRequest(html.editForm(id, formWithErrors))),
        shopData => {
          shopData.asyncUpdateEntity(id).map { shop =>
            Home.flashing("success" -> "Shop %s has been updated".format(shop.name))
          }
        })
    }
  }

  /**
   * Handle shop deletion.
   *
   * @param id Id of the shop to delete
   */
  def delete(id: String) = Authenticated.async {
    asyncTransactionalChain { implicit ctx =>
      asyncById[Shop](id).map { shopOption =>
        shopOption.map { shop =>
          shop.delete
        }.getOrElse(NotFound)
        Home.flashing("success" -> "Shop has been deleted")
      }
    }
  }

  /**
   * Switch theme.
   */
  def selectTheme(theme: String) = Action { implicit request =>
    val redirect = request.headers.get("Referer").map(SeeOther(_)).getOrElse(Home)
    if (theme == "default")
      redirect.withCookies(DiscardingCookie(playThemeCookie).toCookie)
    else {
      val cookie = request.cookies.get(playThemeCookie)
        .map(c => c.copy(value = theme))
        .getOrElse(
          Cookie(name = playThemeCookie, value = theme, maxAge = Some(3600 * 24 * 30)))
      redirect.withCookies(cookie)
    }
  }

  /**
   * Get the current theme from the cookie. If there's no theme stored use "default". 
   */
  def theme(implicit request: RequestHeader): String = request.cookies.get(playThemeCookie).map(_.value).getOrElse(defaultTheme)

}