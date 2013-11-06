package business

import java.util.concurrent.TimeoutException
import models.ProductInfo
import play.api.Logger
import play.api.libs.ws.Response
import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

/**
 * Product search service.
 */
object ProductSearch {

  /**
   * Query the given webshop (async) and return the extracted products.
   */
  def search(query: String, shop: WebShop): Future[List[ProductInfo]] = {

    val start = System.currentTimeMillis()

    shop.search(query, 3000).map { content =>

      Logger.debug(s"Response from ${shop.shortName} took ${System.currentTimeMillis() - start} ms")

      // println("Got response body " + content)
      ProductScraper.extractProducts(content, shop)

    } recover { case e: TimeoutException =>
      Logger.info(s"For ${shop.shortName}: $e")
      Nil
    }
  }
  
}