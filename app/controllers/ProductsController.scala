package controllers

import play.api._
import play.api.cache.Cached
import play.api.mvc._
import play.api.data._
import play.api.data.Forms._
import play.api.libs.ws.WS
import play.api.libs.json._
import play.api.i18n.Messages
import Play.current
import views._
import models._
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import scala.concurrent.Future
import org.apache.commons.lang3.StringUtils
import scala.math.Numeric
import scala.math.Ordering
import scala.collection.immutable.ListMap
import scala.collection.immutable.TreeMap
import scala.collection.Iterable
import business.ProductSearch

object ProductsController extends Controller {

  /**
   * This result directly redirects to the shop list.
   */
  private val Home = Redirect(routes.ProductsController.search())

  /**
   * Handle default path requests, redirect to search
   */
  def index = Action { Home }

  /**
   * Search in all active shops with the given query and display found products.
   */
  def search(query: String, sortBy: ProductsSorting = SortByIndex) = Action.async { implicit request =>
    searchProducts(query, sortBy).map(products => Ok(html.productList(products, ProductsSorting.options, query, sortBy)))
  }

  def liveSearch(query: String, sortBy: ProductsSorting = SortByIndex) = Action.async { implicit request =>
    searchProducts(query, sortBy).map(products => {
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

  def jsRoutes = Cached("jsroutes") {
    Action { implicit request =>
      val jsRoutes = Routes.javascriptRouter("jsRoutes")(
        routes.javascript.ProductsController.search,
        routes.javascript.ProductsController.liveSearch
      )
      // Plain, not require.js'ed:
      // Ok(jsRoutes).as(JAVASCRIPT)
      // For require.js using "define"
      Ok(s"define(function () { $jsRoutes; return jsRoutes; });").as(JAVASCRIPT)
    }
  }

  private def searchProducts(query: String, sortBy: ProductsSorting): Future[Iterable[models.ProductInfo]] = {
    for {
      shops <- Shop.findActiveMapped(shop => shop.scrapingDescription)
      products <- Future.sequence(shops.map(shop =>
        ProductSearch.search(query, shop).map(products => products.filter(p => !StringUtils.isEmpty(p.imageUrl)))))
    } yield sortBy.sort(products)
  }

}