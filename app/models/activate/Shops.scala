package models.activate

import java.util.Date
import shopPersistenceContext._
import scales.xml.jaxen.ScalesXPath
import _root_.models.ScrapingDescription
import models.Page
import net.fwbrasil.radon.transaction.TransactionalExecutionContext
import scala.concurrent.Future
import models.WebShop
import play.api.libs.ws.WS
import scala.collection.immutable.Map

case class ShopScrapingDescription(queryUrlTemplate: String, imageUrlBase: Option[String],
  itemXPath: ScalesXPath, nameXPath: ScalesXPath, priceXPath: ScalesXPath,
  imageUrlXPath: ScalesXPath, detailsUrlXPath: ScalesXPath) extends WebShop {

  def search(query: String) = {
    // "http://www.fcsp-shop.com/advanced_search_result.php?keywords={query}"
    ShopScrapingDescription.parseQueryUrlTemplate(queryUrlTemplate).map { case (url, queryParams, searchParam) =>
      WS.url(url)
      .withQueryString(queryParams:_*)
      .withQueryString(searchParam -> query)
      .withHeaders("Accept-Language" -> "de,en")
      .get
    }.getOrElse(throw new IllegalStateException("Unsupported queryUrlTemplate: " + queryUrlTemplate))
  }

}

object ShopScrapingDescription {
  
  private val queryUrlPattern = "(.*)\\?(?:(.*)&)?(.+)=\\{query\\}".r
  private[activate] def parseQueryUrlTemplate(queryUrlTemplate: String): Option[(String, List[(String, String)], String)] = queryUrlTemplate match {
    case queryUrlPattern(url, queryParamsString, queryParam) => {
      val tokens = if(queryParamsString == null) Nil else
        queryParamsString.split("&").toList.map{ queryParam =>
          val keyValue = queryParam.split("=")
          (keyValue(0), keyValue(1))
        }
      Some((url, tokens, queryParam))
    }
    case _ => None
  }

  private def localXPath(xpath: String) = ScalesXPath(xpath).withNameConversion(ScalesXPath.localOnly)
  
  def apply(shop: Shop): ShopScrapingDescription = ShopScrapingDescription(
      queryUrlTemplate = shop.queryUrlTemplate,
      imageUrlBase = shop.imageUrlBase,
      itemXPath = localXPath(shop.itemXPath), nameXPath = localXPath(shop.nameXPath),
      priceXPath = localXPath(shop.priceXPath), imageUrlXPath = localXPath(shop.imageUrlXPath),
      detailsUrlXPath = localXPath(shop.detailsUrlXPath))
}

@Alias("shop")
class Shop(
  var name: String,
  var url: String,
  var queryUrlTemplate: String,
  var imageUrlBase: Option[String],
  var active: Boolean = false,
  var itemXPath: String,
  var nameXPath: String,
  var priceXPath: String,
  var imageUrlXPath: String,
  var detailsUrlXPath: String)
  extends Entity {

  def scrapingDescription = ShopScrapingDescription(this)

}

object Shop {
  
  def findActive(implicit ctx: TransactionalExecutionContext): Future[Seq[Shop]] = {
    asyncQuery { (s: Shop) => where(s.active :== true) select (s) orderBy (s.id) }
  }

  def list(page: Int = 0, pageSize: Int = 10, orderBy: Int = 2, filter: String = "*")(implicit ctx: TransactionalExecutionContext): Future[Page[Shop]] = {
    val pagination = asyncPaginatedQuery { (s: Shop) =>
      where(toUpperCase(s.name) like filter.toUpperCase) select (s) orderBy {
        orderBy match {
          case -2 =>
            s.name desc
          case -3 =>
            s.active desc
          case 2 =>
            s.name
          case 3 =>
            s.active
        }
      }
    }

    pagination.navigator(pageSize).flatMap { navigator =>
      if (navigator.numberOfResults > 0)
        navigator.page(page).map(p => Page(p, page, page * pageSize, navigator.numberOfResults))
      else
        Future(Page(Nil, 0, 0, 0))
    }
  }

}

