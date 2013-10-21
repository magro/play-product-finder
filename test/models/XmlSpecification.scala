package models

import org.specs2.mutable._
import org.specs2.runner._
import org.junit.runner._
import play.api.test._
import play.api.test.Helpers._
import models.activate._
import net.fwbrasil.activate.test._

@RunWith(classOf[JUnitRunner])
class XmlSpecification extends Specification with ActivateTest {

  override def strategy: Strategy = cleanDatabaseStrategy
  override def context(app: play.api.Application) = shopPersistenceContext

  // --

  "ProductScraper" should {

    "extract product infos for fcspShop" inActivate {

      val contentFcSpShop = """
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

      val items = ProductScraper.extractProducts(contentFcSpShop, SeedShops.fcspShop.scrapingDescription)
      items must haveSize(1)
      val item = items(0)
      item.name must equalTo("Feuerzeug Logo")
      item.price must equalTo(2.5)
      item.imageUrl must equalTo("http://www.fcsp-shop.com/images/product_images/thumbnail_images/SP203153_1.jpg")
      item.detailsUrl must equalTo("http://www.fcsp-shop.com/ZUBEHOeR/Essen-Trinken-Rauchen/Feuerzeug-Logo::1224.html?XTCsid=73b360a5a147352dbdc7ef0fa1820db4")
    }

    "extract name with quotation marks for kiezkicker" inActivate {

      val contentKiezkicker = """
      <html xmlns="http://www.w3.org/1999/xhtml" xml:lang="en">
        <head/>
        <body>
          <ul>
            <li class="item first">
              <a href="http://www.kiezkicker-hamburg.de/feuerzeug-kiezkicker?___SID=U" title="Feuerzeug &quot;Kiezkicker&quot;" class="product-image">
                <img src="http://www.kiezkicker-hamburg.de/media/catalog/product/cache/1/small_image/245x/9df78eab33525d08d6e5fb8d27136e95/f/e/feuer_sw_gr1.jpg" width="245" height="245" alt="Feuerzeug &quot;Kiezkicker&quot;">
              </a>
              <div class="productdetails">
                <a href="http://www.kiezkicker-hamburg.de/feuerzeug-kiezkicker?___SID=U" title="Feuerzeug &quot;Kiezkicker&quot;">
                  <h2 class="product-name">Feuerzeug &quot;Kiezkicker&quot;</h2>
                  <p style="text-align:center;">
                    <br><br><span class="price">1,50&nbsp;€</span>
                  </p>
                </a>
              </div>
            </li>
          </ul>
        </body>
      </html>
      """

      val items = ProductScraper.extractProducts(contentKiezkicker, SeedShops.kiezkicker.scrapingDescription)
      items must haveSize(1)
      val item = items(0)
      item.name must equalTo("""Feuerzeug "Kiezkicker"""")
      item.price must equalTo(1.5)
      item.imageUrl must equalTo("http://www.kiezkicker-hamburg.de/media/catalog/product/cache/1/small_image/245x/9df78eab33525d08d6e5fb8d27136e95/f/e/feuer_sw_gr1.jpg")
      item.detailsUrl must equalTo("http://www.kiezkicker-hamburg.de/feuerzeug-kiezkicker?___SID=U")
    }
  }

}