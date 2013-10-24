package controllers

import play.api._
import play.api.mvc._
import play.api.data._
import play.api.data.Forms._
import play.api.libs.ws.WS
import play.api.libs.json._
import play.api.i18n.Messages
import views._
import models._
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import scala.concurrent.Future

object ProductsController extends Controller {

  /**
   * Search in all active shops with the given query and display found products.
   */
  def search(query: String) = Action.async { implicit request =>
    searchProducts(query).map(products => Ok(html.productList(products, "orderBy", query)))
  }

  def liveSearch(query: String) = Action.async { implicit request =>
    searchProducts(query).map(products => {
      val htmlBySelector = Map(
        "#homeTitle.text" -> Messages("products.content.title", products.size),
        "#content" -> views.html.productListComponent.render(products, query).body)
      val json = Json.obj(
        "success" -> true,
        "query" -> query,
        "htmlBySelector" -> htmlBySelector)
      Ok(json)
    })
  }

  def jsRoutes = Action { implicit request =>
    Ok(Routes.javascriptRouter("jsRoutes")(
      routes.javascript.ProductsController.search,
      routes.javascript.ProductsController.liveSearch)).as("text/javascript")
  }

  private def searchProducts(query: String): Future[Seq[models.ProductInfo]] = {
    val futureProducts = for {
      shops <- WebShops.findActive
      products <- Future.sequence(shops.map(ProductScraper.search(query, _)))
    } yield products.flatten
    futureProducts
  }

}