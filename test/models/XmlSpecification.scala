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

      val items = ProductScraper.extractProducts(content, SeedShops.fcspShop.scrapingDescription)
      items must haveSize(1)
      val item = items(0)
      item.name must equalTo("Feuerzeug Logo")
      item.price.getAmount.doubleValue() must equalTo(2.5)
      item.imageUrl must equalTo("http://www.fcsp-shop.com/images/product_images/thumbnail_images/SP203153_1.jpg")
      item.detailsUrl must equalTo("http://www.fcsp-shop.com/ZUBEHOeR/Essen-Trinken-Rauchen/Feuerzeug-Logo::1224.html?XTCsid=73b360a5a147352dbdc7ef0fa1820db4")
    }

    "extract name with quotation marks for kiezkicker" inActivate {

      val content = """
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

      val items = ProductScraper.extractProducts(content, SeedShops.kiezkicker.scrapingDescription)
      items must haveSize(1)
      val item = items(0)
      item.name must equalTo("""Feuerzeug "Kiezkicker"""")
      item.price.getAmount.doubleValue() must equalTo(1.5)
      item.imageUrl must equalTo("http://www.kiezkicker-hamburg.de/media/catalog/product/cache/1/small_image/245x/9df78eab33525d08d6e5fb8d27136e95/f/e/feuer_sw_gr1.jpg")
      item.detailsUrl must equalTo("http://www.kiezkicker-hamburg.de/feuerzeug-kiezkicker?___SID=U")
    }

    "extract name with quotation marks for nixGut" inActivate {

      val content = """
      <html xmlns="http://www.w3.org/1999/xhtml" xml:lang="en">
        <head/>
        <body>
          <div class="productListing1ColBody">
            <div class="floatbox">
              <div class="productListing1ColBodyImg">
                <a href="http://www.nixgut-onlineshop.de/product_info.php?products_id=3430">
                  <img src="images/product_images/thumbnail_images/image_47085_1.jpg" alt="St.Pauli - Totenkopf" class="thumb_img">
                </a>
              </div>
              <div class="productListing1ColBodyTxt">
                <h1 class="productListing1ColHead">
                  <a href="http://www.nixgut-onlineshop.de/product_info.php?products_id=3430">St.Pauli - Totenkopf</a>
                </h1>
                <div class="productListing1ColDesc">Baby-Body</div>
                <div class="productListing1ColPriceInfo"> 14,00 EUR<br>
                  <span class="productListing1ColTaxInfo">inkl. 19 % MwSt. zzgl.
                    <a target="_blank" href="http://www.nixgut-onlineshop.de/popup_content.php?coID=1&amp;KeepThis=true&amp;TB_iframe=true&amp;height=400&amp;width=600" title="Information" class="thickbox">Versandkosten</a>
                  </span>
                </div>
                <div class="productListing1ColButtons">
                  <span class="productListing1ColViewButton">
                    <a href="http://www.nixgut-onlineshop.de/product_info.php?products_id=3430">
                      <img src="templates/nix-gut/buttons/german/small_view.gif" alt="St.Pauli - Totenkopf">
                    </a>
                  </span>
                </div>
              </div>
            </div>
          </div>
        </body>
      </html>
                    """

      val items = ProductScraper.extractProducts(content, SeedShops.nixgut.scrapingDescription)
      items must haveSize(1)
      val item = items(0)
      item.name must equalTo("""St.Pauli - Totenkopf""")
      item.price.getAmount.doubleValue() must equalTo(14)
      item.imageUrl must equalTo("http://www.nixgut-onlineshop.de/images/product_images/thumbnail_images/image_47085_1.jpg")
      item.detailsUrl must equalTo("http://www.nixgut-onlineshop.de/product_info.php?products_id=3430")
    }
  }

}