package models

import org.specs2.mutable._
import org.specs2.runner._
import org.junit.runner._
import play.api.test._
import play.api.test.Helpers._
import net.fwbrasil.activate.test._
import shopPersistenceContext._

@RunWith(classOf[JUnitRunner])
class ShopsSpec extends Specification with AsyncActivateTest 
	with DefaultAwaitTimeout with FutureAwaits {
  
  // -- Date helpers

  def dateIs(date: java.util.Date, str: String) = (new java.text.SimpleDateFormat("yyyy-MM-dd")).format(date) == str

  // --

  override def strategy: Strategy = asyncCleanDatabaseStrategy
  override def context(app: play.api.Application) = shopPersistenceContext

  // reinitializeContext

  "Shop model" should {
    
    def newShop(name: String, active: Boolean = false) = new Shop(name, None, "url", "queryUrlTemplate", None, None, None, active, "item", "name", "price", "imageUrl", "detailsUrl")

    "be created and retrieved by id" inActivate {
      import shopPersistenceContext._
      val item = transactional(newShop("shop1"))
      transactional {
        val Some(shop) = byId[Shop](item.id)
        shop.name must equalTo("shop1")
      }
    }

    "be created and retrieved by id async" inActivate {
      import shopPersistenceContext._
      val item = transactional(newShop("shop1"))
      val shop = await(asyncTransactionalChain { implicit ctx =>
        asyncById[Shop](item.id)
      })
      shop must beSome(item)
    }

    "be created and selected async" inActivate {
      import shopPersistenceContext._
      val item = transactional(newShop("shop1"))
      val shops = await(asyncTransactionalChain { implicit ctx =>
        asyncSelect[Shop] where(_.id :== item.id)
      })
      shops must contain(item)
    }

    "be created and queried async" inActivate {
      import shopPersistenceContext._
      val item = transactional(newShop("shop1"))
      val shops = await(asyncTransactionalChain { implicit ctx =>
        asyncQuery { (s: Shop) => where(s.id :== item.id) select (s) }
      })
      shops must contain(item)
    }

    "findAll active async" inActivate {
      import shopPersistenceContext._
      val (s1, s2) = transactional {
        (newShop("shop1", true), newShop("shop2"))
      }
      val shops = await(asyncTransactionalChain { implicit ctx =>
        Shop.findActive
      })
      shops must have length(1)
      shops must containAllOf(List(s1))
    }

    "be listed async" inActivate {
      import shopPersistenceContext._
      val (s1, s2) = transactional {
        (newShop("shop1"), newShop("shop2"))
      }
      val shops = await(asyncTransactionalChain { implicit ctx =>
        Shop.list()
      })
      shops.items must have length(2)
      shops.items must containAllOf(List(s1, s2))
    }

  }

}