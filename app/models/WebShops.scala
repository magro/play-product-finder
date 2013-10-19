package models

import scales.xml.jaxen.ScalesXPath
import scala.concurrent.Future
import models.activate.Shop

object FcspShop extends WebShop {

  def localXPath(xpath: String) = ScalesXPath(xpath).withNameConversion(ScalesXPath.localOnly)

  val itemXPath = localXPath(s"//body//*[@class='plist-item']")
  val nameXPath = localXPath(s".//a/img[@alt]/@alt")
  val priceXPath = localXPath(s".//*[@class='plist-price']/text()")
  val imageUrlXPath = localXPath(s".//a/img[@data-original]/@data-original")
  val detailsUrlXPath = localXPath(s".//a[@href]/@href")

  val imageUrlBase = Some("http://www.fcsp-shop.com/")

}

trait WebShop extends ScrapingDescription {
  
}

object WebShops {

  import play.api.libs.concurrent.Execution.Implicits.defaultContext
  import activate.shopPersistenceContext._

  def findActive(): Future[Seq[WebShop]] = asyncTransactionalChain { implicit ctx =>
    Shop.findAll.map(_.map(_.scrapingDescription))
  }

}