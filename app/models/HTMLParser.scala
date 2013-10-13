package models

import org.xml.sax.InputSource
import scala.xml._
import parsing._

object Foo extends App {
  val source = Source.fromString(
  "<html><head/><body><div class='main'><span>test</span></div></body></html>")
  println(new HTMLParser().loadXML(source))
}

private class HTMLParser extends NoBindingFactoryAdapter {

  override def loadXML(source: org.xml.sax.InputSource, _p: SAXParser): Node = {
    loadXML(source)
  }

  def loadXML(source: org.xml.sax.InputSource) = {
    import nu.validator.htmlparser.{sax,common}
    import sax.HtmlParser
    import common.XmlViolationPolicy

    val reader = new HtmlParser
    reader.setXmlPolicy(XmlViolationPolicy.ALLOW)
    reader.setContentHandler(this)
    reader.parse(source)
    rootElem
  }
}
object HTMLParser {
  /**
   * Loads a document by URI.
   */
  def loadURI(uri: String) = {
    var html = new HTMLParser
    html.loadXML(new org.xml.sax.InputSource(uri))
  }
  /**
   * Loads a document by URI.
   */
  def parse(content: String) = {
    val source = Source.fromString(content)
    var html = new HTMLParser
    html.loadXML(source)
  }
}

object WebDocument {
  def load(url: String) = new WebDocument(HTMLParser.loadURI(url), url)
  def parse(content: String) = new WebDocument(HTMLParser.parse(content), content)
}

class WebDocument(val document: scala.xml.Node, val url: String) {

  val title = (this.document \\ "title").headOption.map(_.text)

  val body = (this.document \\ "body").head

  val forms = (this.document \\ "form")

  val images = (this.document \\ "img")

  val links = ((this.document \\ "a") ++ (this.document \\ "link")).distinct

  val meta = (this.document \\ "meta")

  val canonical = this.links.
    filter(_.attribute("rel").headOption.isDefined).
    filter(_.attribute("rel").head.text == "canonical").headOption.getOrElse(this.url)

  def meta(name: String) = (this.document \\ "meta").filter(_.attribute("name").headOption.isDefined).
    filter(_.attribute("name").head == name)

  def getElementById(id: String): Option[Node] = (this.document \\ "_").filter(_.attribute("id").head == id).headOption
  def getElementsById(id: String): NodeSeq = (this.document \\ "_").filter(_.attribute("id").map(_.head == id).getOrElse(false))

  def getElementsByClassName(className: String): NodeSeq = (this.document \\ "_").filter {elem =>
    // println("elem " + elem)
    elem.attribute("class").map(_.head.text.split(" ").contains(className)).getOrElse(false)
  }

  def getElementsByClassNames(classNames: List[String]): NodeSeq = (this.document \\ "_").filter {elems =>
    elems.attribute("class").head.text.split(" ").exists(cls => classNames.contains(cls))
  }
}