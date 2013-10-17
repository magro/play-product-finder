package models.activate

import org.specs2.mutable._
import org.specs2.runner._
import org.junit.runner._
import play.api.test._
import play.api.test.Helpers._
import net.fwbrasil.activate.test._
import scala.concurrent.Future

@RunWith(classOf[JUnitRunner])
class ShopsSpec extends Specification with ActivateTest
	with DefaultAwaitTimeout with FutureAwaits {
  
  // -- Date helpers

  def dateIs(date: java.util.Date, str: String) = new java.text.SimpleDateFormat("yyyy-MM-dd").format(date) == str

  // --
  
  override def strategy: Strategy = recreateDatabaseStrategy
  override def context(app: play.api.Application) = shopPersistenceContext

  // reinitializeContext

  "Shop model" should {
    
    def newShop(name: String) = new Shop(name, None, false, "item", "name", "price", "imageUrl", "detailsUrl")

    "be created and retrieved by id" inActivate {
      import shopPersistenceContext._
	  val item = transactional {
        newShop("shop1")
      }
      transactional {
        val Some(shop) = byId[Shop](item.id)
        shop.name must equalTo("shop1")
      }
    }

    "be listed async" inActivate {
      import shopPersistenceContext._
	  val (s1, s2) = await(asyncTransactionalChain { implicit ctx =>
        Future.successful((newShop("shop1"), newShop("shop2")))
      })
      val shops = await(asyncTransactionalChain { implicit ctx =>
        Shop.list()
      })
      shops.items must have length(2)
      shops.items must containAllOf(List(s1, s2))
    }

  }

}