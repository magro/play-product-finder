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

class SeedData extends Migration {

  override def timestamp = 201310190132L

  override def down = {
    customScript {
      all[Shop].foreach(_.delete)
    }
  }

  override def up = {
    customScript {
      new Shop("FC St. Pauli", "http://www.fcsp-shop.com/",
          "http://www.fcsp-shop.com/advanced_search_result.php?keywords={query}",
          Some("http://www.fcsp-shop.com/"),
          false,
          "//body//*[@class='plist-item']", ".//a/img[@alt]/@alt", ".//*[@class='plist-price']/text()",
          ".//a/img[@data-original]/@data-original", ".//a[@href]/@href")
    }
  }
}
