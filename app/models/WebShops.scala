package models

import scales.xml.jaxen.ScalesXPath
import scala.concurrent.Future
import models.activate.Shop

object WebShops {

  import play.api.libs.concurrent.Execution.Implicits.defaultContext
  import activate.shopPersistenceContext._

  def findActive(): Future[Seq[WebShop]] = asyncTransactionalChain { implicit ctx =>
    Shop.findActive.map(_.map(_.scrapingDescription))
  }

}