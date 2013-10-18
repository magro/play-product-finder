package controllers

import play.api._
import play.api.mvc._
import play.api.data._
import play.api.data.Forms._
import views._
import models._
import play.api.libs.ws.WS
import play.api.libs.json._
import java.io.File
import java.nio.file.Paths
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import scala.concurrent.Future

object ProductsController extends Controller {

  /**
   * Search in all active shops with the given query and display found products.
   */
  def search(query: String) = Action.async { implicit request =>
    // val phones = List(Phone(1, "foo", "foo", "snippet", "imageurl"))
    val futureProducts = for {
      shops <- WebShops.findActive
      products <- Future.sequence(shops.map(ProductScraper.search(query, _)))
    } yield products.flatten

    futureProducts.map(products => Ok(html.productList(products, "orderBy", query)))
  }

}