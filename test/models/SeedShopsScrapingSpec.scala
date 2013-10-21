package models

import org.specs2.mutable._
import org.specs2.runner._
import org.junit.runner._
import play.api.test._
import play.api.test.Helpers._
import models.activate.SeedShops
import models.activate.shopPersistenceContext
import net.fwbrasil.activate.test._

@RunWith(classOf[JUnitRunner])
class SeedShopsScrapingSpec extends Specification with ActivateTest with DefaultAwaitTimeout with FutureAwaits {

  override def strategy: Strategy = cleanDatabaseStrategy
  override def context(app: play.api.Application) = shopPersistenceContext

  "ProductScraper" should {

    "extract product infos for fcsp" inActivate {
      val items = await(ProductScraper.search("Feuerzeug", SeedShops.fcspShop.scrapingDescription))
      items.size must be greaterThan(0)
      val item = items(0)
      item.name must contain("Feuerzeug")
      item.price.getAmount.doubleValue() must be greaterThan(1)
      item.imageUrl must startWith("http://www.fcsp-shop.com/images/product_images/thumbnail_images/")
      item.detailsUrl must startWith("http://www.fcsp-shop.com/ZUBEHOeR/Essen-Trinken-Rauchen/")
    }

    "extract product infos for kiezkicker" inActivate {
      // Search for a specific string to find exactly one product ("Feuerzeug 'Kiezkicker'"),
      // to see that the the '<li class="item first"...' is matched correctly (xpath contains(@class, 'item')).
      val items = await(ProductScraper.search("Siegerzigarre", SeedShops.kiezkicker.scrapingDescription))
      items.size must be greaterThan(0)
      val item = items(0)
      item.name must equalTo("""Feuerzeug "Kiezkicker"""")
      item.price.getAmount.doubleValue() must be greaterThan(1)
      item.imageUrl must startWith("http://www.kiezkicker-hamburg.de/media/catalog/product/")
      item.detailsUrl must startWith("http://www.kiezkicker-hamburg.de/")
    }
  }

}