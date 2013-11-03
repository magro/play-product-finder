package business

import models.ProductInfo
import org.joda.money.{CurrencyUnit, Money}
import org.joda.money.format.MoneyFormatterBuilder
import play.api.libs.ws.Response
import scala.concurrent.Future
import scala.xml._
import scales.utils._
import scales.utils.ScalesUtils._
import scales.xml._
import scales.xml.ScalesXml._
import scales.xml.jaxen._
import scales.xml.parser.sax.DefaultSaxSupport
import scales.utils.resources.SimpleUnboundedPool

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

  def extractProducts(content: String, shop: WebShop): List[ProductInfo] = {
    
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

object NuValidatorFactoryPool extends SimpleUnboundedPool[org.xml.sax.XMLReader] with DefaultSaxSupport {
  def create = {

    import nu.validator.htmlparser.{ sax, common }
    import sax.HtmlParser
    import common.XmlViolationPolicy

    val reader = new HtmlParser
    reader.setXmlPolicy(XmlViolationPolicy.ALLOW)
    reader.setXmlnsPolicy(XmlViolationPolicy.ALLOW)
    reader
  }
}