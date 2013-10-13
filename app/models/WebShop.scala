package models

import play.api.libs.ws.WS
import scala.concurrent.Future

object WebShop extends App {

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

import scala.xml._
class RichNode(node: scala.xml.Node) {
  def getElementByClassName(className: String): Option[Node] = (node  \\ "_").filter { elem =>
    // elem.attribute("class").map(_.head.text.split(" ").contains(className)).getOrElse(false)
    (elem \\ "@class") exists (_.text.split(" ").contains(className))
  }.headOption
  def getElementsByClassName(className: String): NodeSeq = (node \\ "_").filter {elem =>
    // println("elem " + elem)
    elem.attribute("class").map(_.head.text.split(" ").contains(className)).getOrElse(false)
  }
}

object Conversions {
  implicit def node2RichNode(node: scala.xml.Node): RichNode = new RichNode(node)
}

object FcspShop {
  
  val pricePattern = "\\s*(\\d+),(\\d+).*".r
//  val itemClass = "product-listing"
//  val nameClass = "name"
//  val priceClass = "preis"
  val itemClass = "plist-item"
  val nameClass = "plist-item-inside"
  val priceClass = "plist-price"

  def search(query: String): Future[Seq[ProductInfo]] = {
    implicit val context = scala.concurrent.ExecutionContext.Implicits.global
    import Conversions._
    val respFuture = WS.url("http://www.fcsp-shop.com/advanced_search_result.php")
    	.withQueryString("keywords" -> query)
    	.withHeaders("Accept-Language" -> "de,en")
    	.get
    respFuture.map { resp =>
      println("Got response body " + resp.body)
      val doc = WebDocument.parse(resp.body)
      val products = doc.getElementsByClassName(itemClass).map{ elem =>
        val node = new RichNode(elem)
        val name = node.getElementByClassName(nameClass).map(_.text).getOrElse{
          (elem \\ "img").headOption.flatMap(_.attribute("alt").map(_.head.text)).getOrElse("")
        }
        val price = node.getElementByClassName("preis").map( e => e.text match {
          case pricePattern(f, d) => Integer.parseInt(f) + Integer.parseInt(d)/100
          case _ => 0.0
        } ).getOrElse(0.0)
        ProductInfo(name, price, "", "")
      }
      products
    }
  }
}

case class ProductInfo(val name: String, val price: Double, val imageUrl: String, val detailsUrl: String)