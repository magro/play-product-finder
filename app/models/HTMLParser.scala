package models

import org.xml.sax.InputSource
import scala.xml._
import parsing._

import scales.utils._
import scales.utils.ScalesUtils._
import scales.xml._
import scales.xml.ScalesXml._
import scales.xml.jaxen._
import scales.utils.resources.SimpleUnboundedPool
import scales.xml.parser.sax.DefaultSaxSupport

object Foo extends App {
  val source = Source.fromString(
  """<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
     <html xmlns="http://www.w3.org/1999/xhtml" dir="ltr" xml:lang="de"><head/><body><div id="content"><div class="main">foo</div></div></body></html>""")
  
//  val xml = new HTMLParser().loadXML(source)
//  println(xml)
    
  val doc = loadXmlReader(source, strategy = defaultPathOptimisation, parsers = NuValidatorFactoryPool)
  val root = top(doc)
  // println(root)
     
    /*
    val strPath = ScalesXPath("/ns:html/ns:body/ns:p[2]/ns:table[2]/ns:tbody/ns:tr/ns:td/ns:table/ns:tbody/ns:tr/ns:td[1]/ns:a/ns:font/text()", 
		ns.prefixed("ns")) 

	// all the 9 text nodes 
	val strRes = strPath.evaluate(root) 
      */
  val ns = Namespace("http://www.w3.org/1999/xhtml")
  
  println(ScalesXPath("//body//*[@class='main']/@class").withNameConversion(ScalesXPath.localOnly).evaluate(root).head.left.get.attribute.value)
  println(ScalesXPath("//body//*[@class='main']/text()").withNameConversion(ScalesXPath.localOnly).evaluate(root).head.right.get.item.value)
  
  val ctxt = ScalesXPath("//body/div").withNameConversion(ScalesXPath.localOnly).xmlPaths(root).head
  println(ScalesXPath("//*[@class='main']/text()").withNameConversion(ScalesXPath.localOnly).evaluate(ctxt).head.right.get.item.value)
  
//  val ns = Namespace("http://www.w3.org/1999/xhtml") 
//  println(ScalesXPath("//body", ns.prefixed("")).evaluate(root))
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