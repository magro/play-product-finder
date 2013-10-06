package models.activate

import org.specs2.mutable._
import org.specs2.runner._
import org.junit.runner._
import play.api.test._
import play.api.test.Helpers._
// import models.activate.computerPersistenceContext._
import net.fwbrasil.activate.statement.Criteria
import net.fwbrasil.activate.entity.map.MutableEntityMap
import net.fwbrasil.activate.test._

@RunWith(classOf[JUnitRunner])
class ProductsSpec extends Specification with ActivateTest {
  
  import models.activate._

  // -- Date helpers

  def dateIs(date: java.util.Date, str: String) = new java.text.SimpleDateFormat("yyyy-MM-dd").format(date) == str

  // --
  
  override def strategy: Strategy = cleanDatabaseStrategy
  override def context(app: play.api.Application) = computerPersistenceContext

  "Category model" should {
    
    "be created and retrieved by id" inActivate {
      import computerPersistenceContext._
	  val item = transactional {
        new Category("Phones", Some("description"))
      }
      transactional {
        byId[Category](item.id) must beSome
      }
    }

    "list options" inActivate {
      import computerPersistenceContext._
	  val (c1, c2) = transactional {
        (new Category("Phones"), new Category("Computers"))
      }
      transactional {
        def option(c: Category) = (c.id, c.name)

        val options = Category.options
        options must have length(2)
        options must containAllOf(List(option(c1), option(c2)))
      }
    }

    "be updated if needed" inActivateWithSeed(() => new Category("Phone", Some("description")), {
      import computerPersistenceContext._
	  val cId = transactional {
        val c = select[Category].where(_.name :== "Phone").head
        c.name = "Phones"
        c.id
      }
      transactional {
        val Some(c) = byId[Category](cId)
        c.name must equalTo("Phones")
      }
    })
  }
  
  "Brand model" should {
    
    "be created and retrieved by id" inActivate {
      import computerPersistenceContext._
	  val item = transactional {
        new Brand("Samsung", Some("description"))
      }
      transactional {
        byId[Brand](item.id) must beSome
      }
    }

    "list options" inActivate {
      import computerPersistenceContext._
	  val (b1, b2) = transactional {
        (new Brand("Samsung"), new Brand("Motorola"))
      }
      transactional {
        def option(b: Brand) = (b.id, b.name)

        val options = Brand.options
        options must have length(2)
        options must containAllOf(List(option(b1), option(b2)))
      }
    }

  }
  
  
  "Product model" should {
    
    "be created and retrieved by id" inActivate {
      import computerPersistenceContext._
	  val item = transactional {
        new Product(new Brand("Samsung"), new Category("Phones"), "galaxy", "snippet", "description", "imageurl", Nil)
      }
      transactional {
        val Some(found) = byId[Product](item.id)
        found must equalTo(item)
      }
    }
    
    "return brand and category" inActivate {
      import computerPersistenceContext._
	  val (c, b, p) = transactional {
        val c = new Category("Phones")
        val b = new Brand("Samsung")
        val p = new Product(b, c, "galaxy", "snippet", "description", "imageurl", Nil)
        (c, b, p)
      }
      transactional {
        val Some(product) = byId[Product](p.id)
        product must equalTo(p)
        product.category must equalTo(c)
        product.brand must equalTo(b)
      }
    }

    "find product with attributes" inActivate {
      import computerPersistenceContext._
	  val (p, as) = transactional {
        val cat = new Category("Phones")
        val brand = new Brand("Samsung")
        val a1 = new ProductAttribute("key1", "value1")
        val a2 = new ProductAttribute("key2", "value2")
        val p = new Product(brand, cat, "galaxy", "snippet", "description", "imageurl", attributes = List(a1, a2))
        (p, List(a1, a2))
      }
      transactional {
        val Some(product) = byId[Product](p.id)
        product must equalTo(p)
        product.attributes must have length(2)
        product.attributes must containAllOf(as)
      }
    }

  }

}