package models.activate

import shopPersistenceContext._
import net.fwbrasil.activate.migration.Migration
import scala.collection.mutable.{ Map => MutableMap }
import java.util.Date
import java.text.SimpleDateFormat
import net.fwbrasil.activate.migration.IfExists

class CreateSchema extends Migration {

  override def timestamp = 201310190131L

  override def up = {
    createTableForEntity[Shop].ifNotExists
  }

  override def down = {
    table[Shop].removeTable.ifExists
  }
}

object SeedShops {

  def fcspShop = new Shop("FC St. Pauli", "http://www.fcsp-shop.com/",
          "http://www.fcsp-shop.com/advanced_search_result.php?keywords={query}",
          Some("http://www.fcsp-shop.com/"),
          false,
          "//body//*[@class='plist-item']", ".//a/img[@alt]/@alt", ".//*[@class='plist-price']/text()",
          ".//a/img[@data-original]/@data-original", ".//a[@href]/@href")

  def kiezkicker = new Shop("Kiezkicker Hamburg", "http://www.kiezkicker-hamburg.de/",
          "http://www.kiezkicker-hamburg.de/catalogsearch/result/?q={query}",
          None,
          false,
          "//body//li[contains(@class, 'item')]",
          /* the product-name text node is e.g. 'Feuerzeug &quot;Kiezkicker&quot;' which is parsed as "Feuerzeug "
           * ".//h2[@class='product-name']/text()",
           * so let's read the link title which comes out as expected
           */
          "./a/@title",
           ".//*[@class='price']/text()",
          ".//a[@class='product-image']/img/@src", ".//a[@class='product-image']/@href")
}

class SeedData extends Migration {

  override def timestamp = 201310211343L

  override def down = {
    customScript {
      all[Shop].foreach(_.delete)
    }
  }

  override def up = {
    customScript {
      SeedShops.fcspShop
      SeedShops.kiezkicker
    }
  }
}
