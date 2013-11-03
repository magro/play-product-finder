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
import org.apache.commons.lang3.StringUtils
import scala.math.Numeric
import scala.math.Ordering
import scala.collection.immutable.ListMap
import scala.collection.immutable.TreeMap
import scala.collection.Iterable
import business.ProductSearch


object Foo extends App {
  case class Version(major: Int, minor: Int, bugfix: Int) {
    private val version = "(\\d+)\\.(\\d+)\\.(\\d+)".r
    def apply(v: String): Option[Version] = v match {
      case version(maj, min, fix) => Some(Version(maj.toInt, min.toInt, fix.toInt))
    }
  } 
  
  println("""
          |This project uses Play %1$s while your Play installation uses %2$s!
          |Update the Play sbt-plugin version to %2$s (usually in project/plugins.sbt) or install Play %1$s" 
        """.stripMargin.format("2.2.1", "2.2.0"))
}

object ProductsController extends Controller {

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

  def jsRoutes = Action { implicit request =>
    Ok(Routes.javascriptRouter("jsRoutes")(
      routes.javascript.ProductsController.search,
      routes.javascript.ProductsController.liveSearch)).as("text/javascript")
  }

  private def searchProducts(query: String, sortBy: ProductsSorting): Future[Iterable[models.ProductInfo]] = {
    for {
      shops <- Shop.findActiveMapped(shop => shop.scrapingDescription)
      products <- Future.sequence(
        shops.map(shop =>
          ProductSearch.search(query, shop)
            .map(products => products.filter(p => !StringUtils.isEmpty(p.imageUrl)))))
    } yield sortBy.sort(products)
  }

}