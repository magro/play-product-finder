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

    // Log how long a Future took to process
    def withTiming[T](f: => Future[T]): Future[T] = {
      val start = System.currentTimeMillis()
      f.map {
        case r =>
          val latency = System.currentTimeMillis() - start
          Logger.info(s"Response from ${shop.shortName} took $latency ms")
          r
      }
    }

    withTiming(shop.search(query, 3000)).map( content =>
      ProductScraper.extractProducts(content, shop)
    ) recover { case e: TimeoutException =>
      Logger.info(s"For ${shop.shortName}: $e")
      Nil
    }
  }
  
}