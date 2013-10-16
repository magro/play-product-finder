package models

import org.specs2.mutable._
import org.specs2.runner._
import org.junit.runner._
import play.api.test._
import play.api.test.Helpers._

@RunWith(classOf[JUnitRunner])
class XmlSpecification extends Specification {

  // --

  "ProductScraper" should {

    val content = """
      <html xmlns="http://www.w3.org/1999/xhtml" xml:lang="en">
        <head/>
        <body>
          <div id="content">
            <div class="plist-item">
              <div class="plist-item-inside">
                <a href="http://www.fcsp-shop.com/ZUBEHOeR/Essen-Trinken-Rauchen/Feuerzeug-Logo::1224.html?XTCsid=73b360a5a147352dbdc7ef0fa1820db4">
                  <img src="images/loader.gif" data-original="images/product_images/thumbnail_images/SP203153_1.jpg" alt="Feuerzeug Logo" class="lazy plist-pic"/>
                </a>
                <div class="plist-texter">
                  <p class="plist-price"> 2,50 EUR</p>
                  <p class="plist-head">
                    <a href="http://www.fcsp-shop.com/ZUBEHOeR/Essen-Trinken-Rauchen/Feuerzeug-Logo::1224.html?XTCsid=73b360a5a147352dbdc7ef0fa1820db4">
                      Feuerzeug Logo<br/><span style="font-size: 10px;"></span>
                    </a>
                  </p>
                </div>
                <p class="artnr">SP203153</p>
              </div>
            </div>
          </div>
        </body>
      </html>
    """

    "extract product infos" in {
      val items = ProductScraper.extractProducts(content, FcspShop)
      items must haveSize(1)
      val item = items(0)
      item.name must equalTo("Feuerzeug Logo")
      item.price must equalTo(2.5)
      item.imageUrl must equalTo("http://www.fcsp-shop.com/images/product_images/thumbnail_images/SP203153_1.jpg")
      item.detailsUrl must equalTo("http://www.fcsp-shop.com/ZUBEHOeR/Essen-Trinken-Rauchen/Feuerzeug-Logo::1224.html?XTCsid=73b360a5a147352dbdc7ef0fa1820db4")
    }
  }

}