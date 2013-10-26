package models

import play.api.libs.ws.WS
import scala.concurrent.Future
import scala.xml._
import scales.utils._
import scales.utils.ScalesUtils._
import scales.xml._
import scales.xml.ScalesXml._
import scales.xml.jaxen._
import scales.utils.resources.SimpleUnboundedPool
import scales.xml.parser.sax.DefaultSaxSupport
import play.api.libs.ws.Response
import models.activate.ShopScrapingDescription
import java.util.Currency
import org.joda.money.{CurrencyUnit, Money}
import org.joda.money.format.MoneyFormatterBuilder

object WebShopTest extends App {

//  val node = HTMLParser.loadURI("http://www.fcsp-shop.com/advanced_search_result.php?keywords=hoody")
//  println(node)
  
  // Kommt nichts raus:
  // http://www.fcsp-shop.com/advanced_search_result.php?keywords=hoody
  // Aber hier:
  // http://www.fcsp-shop.com/advanced_search_result.php?keywords=Feuerzeug
  
  /*
   * http://www.rocknshop.de/advanced_search_result.php?keywords=Kapuzenpulli&categories_id=1349&inc_subcat=1
   */
  
  //*
  val doc = WebDocument.load("http://www.fcsp-shop.com/advanced_search_result.php?keywords=Feuerzeug")
  println(doc.document)
  val artnrs = doc.getElementsByClassName("name")
  println("Artnrs: " + artnrs.map(_.text).mkString(", "))
  // */
  
  // println(WebDocument.parse("<html></html>").document)
  
}

trait ScrapingDescription {
  /**
   * The xpath expression to the item/product container
   */
  val itemXPath: ScalesXPath
  /**
   * The xpath expression to locate the name element/attribute, should be relative to itemXPath
   */
  val nameXPath: ScalesXPath
  val priceXPath: ScalesXPath
  val imageUrlXPath: ScalesXPath
  val detailsUrlXPath: ScalesXPath
  
  /**
   * The baseUrl used as prefix for the imageUrl
   */
  val imageUrlBase: Option[String]
}

trait WebShop extends ScrapingDescription {
  def search(query: String): Future[Response]
  val responseEncoding: Option[String] = None
  val shortName: String
}

object ProductScraper {
  
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

  def extractProducts(content: String, shop: WebShop): List[models.ProductInfo] = {
    
    val doc = loadXmlReader(Source.fromString(content), strategy = defaultPathOptimisation, parsers = NuValidatorFactoryPool)
    val root = top(doc)
    
//    println("Got content " + content)
//    
//    sd.itemXPath.evaluate(root).foreach{ item => item match {
//        case Right(xpath) => println("Got name " + queryXPath(xpath, sd.nameXPath).getOrElse("-"))
//        case Left(foo) => println("Got foo " + foo)
//      }
//    }

    val products = shop.itemXPath.evaluate(root).foldLeft(List.empty[ProductInfo]) { (acc, item) =>
      item match {
        case Right(xpath) => {
          val subtree = xpath // top(xpath.tree)
//          println("** " + queryXPath(xpath, ShopScrapingDescription.localXPath("./a/@title")))
          val name = queryXPath(subtree, shop.nameXPath).getOrElse("-")
          val price: Double = queryXPath(subtree, shop.priceXPath).flatMap(parseDouble).getOrElse(0.0)
          val imageUrl = queryXPath(subtree, shop.imageUrlXPath).map { imgUrl =>
            shop.imageUrlBase.map(_ + imgUrl).getOrElse(imgUrl)
          }.getOrElse("")
          val detailsUrl = queryXPath(subtree, shop.detailsUrlXPath).getOrElse("")
          ProductInfo(name, Money.of(CurrencyUnit.EUR, price), imageUrl, detailsUrl, shop.shortName) :: acc
        }
        case Left(_) => acc
      }
    }
    products.reverse
  }
  
  private def queryXPath(context: XmlPath, xpath: ScalesXPath) = xpath.evaluate(context).headOption.map(extractValue)
  
  private def extractValue(evalResult: Either[AttributePath, XmlPath]): String = evalResult match {
    case Left(attributePath) => attributePath.value
    case Right(xmlPath) => xmlPath.item.value
  }
  
  val pricePattern = "\\s*(\\d+),(\\d+).*".r
  private def parseDouble(value: String): Option[Double] = value match {
    case pricePattern(f, d) => Some(f.toInt + d.toDouble/100)
    case _ => None
  }

}

object ProductInfo {
  private val moneyFormatter = new MoneyFormatterBuilder().appendAmountLocalized().appendLiteral(" ").appendCurrencySymbolLocalized().toFormatter()
}

case class ProductInfo(val name: String, val price: Money, val imageUrl: String, val detailsUrl: String, val shopName: String) {

  def priceFormatted(implicit lang: play.api.i18n.Lang) = ProductInfo.moneyFormatter.withLocale(lang.toLocale).print(price)
}