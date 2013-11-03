package models

import java.util.Locale
import org.junit.runner._
import org.specs2.mutable._
import org.specs2.runner._

import net.fwbrasil.activate.test._
import play.api.test._
import play.api.test.Helpers._

import business.ProductSearch

@RunWith(classOf[JUnitRunner])
class ProductSearchSpec extends Specification with ActivateTest with DefaultAwaitTimeout with FutureAwaits {

  override def strategy: Strategy = recreateDatabaseStrategy
  override def context(app: play.api.Application) = shopPersistenceContext

  "ProductSearch" should {

    "extract product infos for fcsp" inActivate {
      val items = await(ProductSearch.search("Feuerzeug", SeedShops.fcspShop.scrapingDescription))
      items.size must be greaterThan(0)
      val item = items(0)
      item.name must contain("Feuerzeug")
      item.price.getAmount.doubleValue() must be greaterThan(1)
      item.imageUrl must startWith("http://www.fcsp-shop.com/images/product_images/thumbnail_images/")
      item.detailsUrl must startWith("http://www.fcsp-shop.com/ZUBEHOeR/Essen-Trinken-Rauchen/")
    }

    "find products by search with umlaut" inActivate {
      val items = await(ProductSearch.search("schwarz-grün", SeedShops.fcspShop.scrapingDescription))
      items.size must be greaterThan(0)
      val item = items(0)
      item.name.toLowerCase(Locale.GERMAN) must contain("grün")
    }

    "extract product infos for kiezkicker" inActivate {
      // Search for a specific string to find exactly one product ("Feuerzeug 'Kiezkicker'"),
      // to see that the the '<li class="item first"...' is matched correctly (xpath contains(@class, 'item')).
      val items = await(ProductSearch.search("Siegerzigarre", SeedShops.kiezkicker.scrapingDescription))
      items.size must be greaterThan(0)
      val item = items(0)
      item.name must equalTo("""Feuerzeug "Kiezkicker"""")
      item.price.getAmount.doubleValue() must be greaterThan(1)
      item.imageUrl must startWith("http://www.kiezkicker-hamburg.de/media/catalog/product/")
      item.detailsUrl must startWith("http://www.kiezkicker-hamburg.de/")
    }

    "extract product infos with umlauts for kiezkicker" inActivate {
      val items = await(ProductSearch.search("blattgrün", SeedShops.kiezkicker.scrapingDescription))
      items.size must be greaterThan(0)
      val item = items(0)
      item.name must contain("blattgrün")
    }

    "extract product infos for nixgut" inActivate {
      val items = await(ProductSearch.search("Baby-Body", SeedShops.nixgut.scrapingDescription))
      items.size must be greaterThan(0)
      val item = items(0)
      item.name must equalTo("""St.Pauli - Totenkopf""")
      item.price.getAmount.doubleValue() must be greaterThan(1)
      item.imageUrl must startWith("http://www.nixgut-onlineshop.de/images/product_images/")
      item.detailsUrl must startWith("http://www.nixgut-onlineshop.de/product_info.php?products_id=")
    }
  }

}