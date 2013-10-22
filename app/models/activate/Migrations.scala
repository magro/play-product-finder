package models.activate

import shopPersistenceContext._
import net.fwbrasil.activate.migration.Migration
import scala.collection.mutable.{ Map => MutableMap }
import java.util.Date
import java.text.SimpleDateFormat
import net.fwbrasil.activate.migration.IfExists

class CreateSchema extends Migration {

  override def timestamp = 201310220131L

  override def up = {
    table[Shop].removeTable.ifExists
    createTableForEntity[Shop].ifNotExists
  }

  override def down = {
    table[Shop].removeTable.ifExists
  }
}

object SeedShops {

  def fcspShop = new Shop("FC St. Pauli", "http://www.fcsp-shop.com/",
          "http://www.fcsp-shop.com/advanced_search_result.php?keywords={query}",
          Some("ISO-8859-15"),
          Some("http://www.fcsp-shop.com/"),
          true,
          "//body//*[@class='plist-item']", ".//a/img[@alt]/@alt",
          ".//*[@class='plist-price']/text()",
          ".//a/img[@data-original]/@data-original",
          ".//a[@href]/@href")

  def kiezkicker = new Shop("Kiezkicker Hamburg", "http://www.kiezkicker-hamburg.de/",
          "http://www.kiezkicker-hamburg.de/catalogsearch/result/?q={query}",
          None,
          None,
          true,
          "//body//li[contains(@class, 'item')]",
          /* the product-name text node is e.g. 'Feuerzeug &quot;Kiezkicker&quot;' which is parsed as "Feuerzeug "
           * ".//h2[@class='product-name']/text()",
           * so let's read the link title which comes out as expected
           */
          "./a/@title",
           ".//*[@class='price']/text()",
          ".//a[@class='product-image']/img/@src",
          ".//a[@class='product-image']/@href")

  def nixgut = new Shop("Nix Gut Mailorder / Alles von St. Pauli", "http://www.nixgut-onlineshop.de/index.php?cPath=13_43",
    "http://www.nixgut-onlineshop.de/advanced_search_result.php?categories_id=43&inc_subcat=1&keywords={query}",
    Some("ISO-8859-15"),
    Some("http://www.nixgut-onlineshop.de/"),
    true,
    "//body//div[@class='productListing1ColBody']",
    ".//div[@class='productListing1ColBodyImg']/a/img/@alt",
    ".//div[@class='productListing1ColPriceInfo']/text()",
    ".//div[@class='productListing1ColBodyImg']/a/img/@src",
    ".//div[@class='productListing1ColBodyImg']/a/@href")
}

class SeedData extends Migration {

  override def timestamp = 201310221344L

  override def down = {
    customScript {
      all[Shop].foreach(_.delete)
    }
  }

  override def up = {
    customScript {
      all[Shop].foreach(_.delete)
      SeedShops.fcspShop
      SeedShops.kiezkicker
      SeedShops.nixgut
    }
  }
}
