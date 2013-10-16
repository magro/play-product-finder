package models

import scales.xml.jaxen.ScalesXPath
import scala.concurrent.Future

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
  def findActive(): Future[Seq[WebShop]] = Future.successful(List(FcspShop))
}