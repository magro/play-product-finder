package models

import business.WebShop
import shopPersistenceContext._
import scales.xml.jaxen.ScalesXPath
import net.fwbrasil.radon.transaction.TransactionalExecutionContext
import scala.concurrent.Future
import play.api.libs.ws.WS
import play.api.Logger

case class ShopScrapingDescription(shortName: String, queryUrlTemplate: String, queryUrlEncoding: Option[String] = None,
  override val responseEncoding: Option[String] = None, imageUrlBase: Option[String] = None,
  itemXPath: ScalesXPath, nameXPath: ScalesXPath, priceXPath: ScalesXPath,
  imageUrlXPath: ScalesXPath, detailsUrlXPath: ScalesXPath) extends WebShop {

  def search(query: String) = {
    // "http://www.fcsp-shop.com/advanced_search_result.php?keywords={query}"
    ShopScrapingDescription.parseQueryUrlTemplate(queryUrlTemplate).map { case (url, queryParams, searchParam) =>
      val ws = WS.url(url)
        .withQueryString(queryParams:_*)
        .withQueryString(searchParam -> queryUrlEncoding.map(enc => java.net.URLEncoder.encode(query, enc)).getOrElse(query))
        .withHeaders("Accept-Language" -> "de,en")

      Logger.debug("Requesting " + ws.toString)

      ws.get
    }.getOrElse(throw new IllegalStateException("Unsupported queryUrlTemplate: " + queryUrlTemplate))
  }

}

object ShopScrapingDescription {
  
  private val queryUrlPattern = "(.*)\\?(?:(.*)&)?(.+)=\\{query\\}".r
  private[models] def parseQueryUrlTemplate(queryUrlTemplate: String): Option[(String, List[(String, String)], String)] = queryUrlTemplate match {
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
      shortName = shop.shortName.getOrElse(shop.name),
      queryUrlTemplate = shop.queryUrlTemplate,
      queryUrlEncoding = shop.queryUrlEncoding,
      responseEncoding = shop.responseEncoding,
      imageUrlBase = shop.imageUrlBase,
      itemXPath = localXPath(shop.itemXPath), nameXPath = localXPath(shop.nameXPath),
      priceXPath = localXPath(shop.priceXPath), imageUrlXPath = localXPath(shop.imageUrlXPath),
      detailsUrlXPath = localXPath(shop.detailsUrlXPath))
}

/**
 * Helper for pagination.
 */
case class Page[A](items: Seq[A], page: Int, offset: Long, total: Long) {
  lazy val prev = Option(page - 1).filter(_ >= 0)
  lazy val next = Option(page + 1).filter(_ => (offset + items.size) < total)
}

class Shop(
  var name: String,
  var shortName: Option[String],
  var url: String,
  var queryUrlTemplate: String,
  var queryUrlEncoding: Option[String],
  var responseEncoding: Option[String],
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
  
  // import play.api.libs.concurrent.Execution.Implicits.defaultContext

  def findActiveMapped[T](f: Shop => T): Future[List[T]] = asyncTransactionalChain { implicit ctx =>
    findActive(ctx).map(_.map(f))
  }
  
  def findActive(implicit ctx: TransactionalExecutionContext): Future[List[Shop]] = {
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

