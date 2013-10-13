package models

import org.specs2.mutable._
import org.specs2.runner._
import org.junit.runner._
import play.api.test._
import play.api.test.Helpers._

@RunWith(classOf[JUnitRunner])
class XmlSpecification extends Specification {

  // --

  "RichNode" should {

    "get element by class name" in {
      val xml = <html><head/><body><div><p class="a">foo</p></div></body></html>
      val cut = new RichNode(xml)
      val actual = cut.getElementByClassName("a")
      actual should beSome
      actual.get.text must equalTo("foo")
    }

  }

}