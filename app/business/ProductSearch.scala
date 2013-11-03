package business

import play.api.libs.ws.Response
import scala.concurrent.Future
import models.ProductInfo

/**
 * Product search service.
 */
object ProductSearch {

  /**
   * Query the given webshop (async) and return the extracted products.
   */
  def search(query: String, shop: WebShop): Future[List[ProductInfo]] = {

    implicit val context = scala.concurrent.ExecutionContext.Implicits.global

    shop.search(query).map { resp =>
      val content = bodyWithShopEncoding(resp, shop)
      // println("Got response body " + resp.body)
      ProductScraper.extractProducts(content, shop)
    }
  }

  private def bodyWithShopEncoding(resp: Response, shop: WebShop): String = {
    shop.responseEncoding.map(charset => resp.ahcResponse.getResponseBody(charset)).getOrElse(resp.body)
  }
  
}