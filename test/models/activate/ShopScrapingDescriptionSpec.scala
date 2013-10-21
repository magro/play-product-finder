package models.activate

import org.specs2.mutable._
import org.specs2.runner._
import org.junit.runner._
import play.api.test._
import play.api.test.Helpers._
import net.fwbrasil.activate.test._
import scala.concurrent.Future

@RunWith(classOf[JUnitRunner])
class ShopScrapingDescriptionSpec extends Specification {

  "ShopScrapingDescription" should {

    "extract url and queryParam from queryUrlTemplate" in {
      val url = "http://www.fcsp-shop.com/advanced_search_result.php"
      val queryParam = "keywords"
      val result = ShopScrapingDescription.parseQueryUrlTemplate(s"$url?$queryParam={query}")
      result must beSome((url, queryParam))
    }

  }
}