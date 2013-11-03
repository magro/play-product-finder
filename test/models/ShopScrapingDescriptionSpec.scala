package models

import org.specs2.mutable._
import org.specs2.runner._
import org.junit.runner._
import play.api.test._
import play.api.test.Helpers._
import net.fwbrasil.activate.test._

@RunWith(classOf[JUnitRunner])
class ShopScrapingDescriptionSpec extends Specification {

  "ShopScrapingDescription" should {

    "extract url and queryParam from simple queryUrlTemplate" in {
      val url = "http://www.fcsp-shop.com/advanced_search_result.php"
      val queryParam = "keywords"
      val result = ShopScrapingDescription.parseQueryUrlTemplate(s"$url?$queryParam={query}")
      result must beSome((url, Nil, queryParam))
    }

    "extract url and queryParam from complex queryUrlTemplate" in {
      val Some((url, queryParams, searchParam)) = ShopScrapingDescription.parseQueryUrlTemplate(
          "http://www.nixgut-onlineshop.de/advanced_search_result.php?categories_id=43&inc_subcat=1&keywords={query}")
      url must equalTo("http://www.nixgut-onlineshop.de/advanced_search_result.php")
      queryParams must containAllOf(List(("categories_id", "43"), ("inc_subcat", "1")))
      searchParam must equalTo("keywords")
    }

  }
}