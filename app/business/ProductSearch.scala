package business

import java.util.concurrent.TimeoutException
import models.ProductInfo
import play.api.Logger
import play.api.libs.ws.Response
import scala.concurrent.Future

/**
 * Product search service.
 */
object ProductSearch {

  /**
   * Query the given webshop (async) and return the extracted products.
   */
  def search(query: String, shop: WebShop): Future[List[ProductInfo]] = {

    implicit val context = scala.concurrent.ExecutionContext.Implicits.global
    val start = System.currentTimeMillis()

    shop.search(query, 3000).map { resp =>

      Logger.debug(s"Response from ${shop.shortName} took ${System.currentTimeMillis() - start} ms")

      val content = bodyWithShopEncoding(resp, shop)
      // println("Got response body " + resp.body)
      ProductScraper.extractProducts(content, shop)

    } recover { case e: TimeoutException =>
      Logger.info(s"For ${shop.shortName}: $e")
      Nil
    }
  }

  private def bodyWithShopEncoding(resp: Response, shop: WebShop): String = {
    shop.responseEncoding.map(charset => resp.ahcResponse.getResponseBody(charset)).getOrElse(resp.body)
  }
  
}