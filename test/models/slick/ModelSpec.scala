package models.slick

import org.specs2.mutable._
import org.specs2.runner._
import org.junit.runner._

import play.api.test._
import play.api.test.Helpers._

import play.api.db.slick.DB
import slick.session.Session
import play.api.Play.current

import models._

@RunWith(classOf[JUnitRunner])
class SlickModelSpec extends Specification {
  
  args(skipAll = true)
  
  import models._

  // -- Date helpers
  
  def dateIs(date: java.util.Date, str: String) = new java.text.SimpleDateFormat("yyyy-MM-dd").format(date) == str
  
  // --
  
  "Category model" should {
    
    "be created and retrieved by id" in {
      running(FakeApplication(additionalConfiguration = inMemoryDatabase())) {
        DB.withSession{ implicit s:Session =>
          val c = Categories.insert(Category(None, "Phones", Some("description")))
          c.id must beSome
          
          val Some(cat) = Categories.findById(c.id.get)
          cat must equalTo(c)
        }
        
      }
    }

    "list options" in {
      running(FakeApplication(additionalConfiguration = inMemoryDatabase())) {
        DB.withSession{ implicit s:Session =>
          val c1 = Categories.insert(Category(None, "Phones"))
          val c2 = Categories.insert(Category(None, "Computers"))
          
          def option(c: Category) = (c.id.get.toString, c.name)

          val options = Categories.options
          options must have length(2)
          options must containAllOf(List(option(c1), option(c2)))
        }
      }
    }

    "be updated if needed" in {
      running(FakeApplication(additionalConfiguration = inMemoryDatabase())) {
        DB.withSession { implicit s: Session =>
          val c = Categories.insert(Category(None, "Phone", Some("description")))

          Categories.update(c.id.get, Category(name = "Phones"))

          val Some(cat) = Categories.findById(c.id.get)
          cat.name must equalTo("Phones")
        }
      }
    }
  }
  
  "Brand model" should {
    
    "be created and retrieved by id" in {
      running(FakeApplication(additionalConfiguration = inMemoryDatabase())) {
        DB.withSession{ implicit s:Session =>
          val item = Brands.insert(Brand(None, "Samsung", Some("description")))
          item.id must beSome
          
          val Some(found) = Brands.findById(item.id.get)
          found must equalTo(item)
        }
        
      }
    }

    "list options" in {
      running(FakeApplication(additionalConfiguration = inMemoryDatabase())) {
        DB.withSession{ implicit s:Session =>
          val item1 = Brands.insert(Brand(None, "Samsung"))
          val item2 = Brands.insert(Brand(None, "Motorola"))
          
          def option(c: Brand) = (c.id.get.toString, c.name)

          val options = Brands.options
          options must have length(2)
          options must containAllOf(List(option(item1), option(item2)))
        }
      }
    }
  }
  
  
  "Product model" should {
    
    "be created and retrieved by id" in {
      running(FakeApplication(additionalConfiguration = inMemoryDatabase())) {
        DB.withSession{ implicit s:Session =>
          val cat = Categories.insert(Category(None, "Phones"))
          val brand = Brands.insert(Brand(None, "Samsung"))
          val item = Products.insert(Product(None, brand.id.get, cat.id.get, "galaxy", "snippet", "description", "imageurl", Nil))
          item.id must beSome
          
          val Some(found) = Products.findById(item.id.get)
          found must equalTo(item)
        }
      }
    }
    
    "return brand and category" in {
      running(FakeApplication(additionalConfiguration = inMemoryDatabase())) {
        DB.withSession{ implicit s:Session =>
//          val cat = Categories.insert(Category(None, "Phones"))
//          val brand = Brands.insert(Brand(None, "Samsung"))
//          val item = Products.insert(Product(None, brand.id.get, cat.id.get, "galaxy", "snippet", "description", "imageurl", Nil))
//          
//          val Some(found) = Products.findById(item.id.get)
//          found.category must equalTo(cat)
//          found.brand must equalTo(brand)
          
          skipped
        }
      }
    } 

    "find product with attributes" in {
      running(FakeApplication(additionalConfiguration = inMemoryDatabase())) {
        DB.withSession{ implicit s:Session =>
          val cat = Categories.insert(Category(None, "Phones"))
          val brand = Brands.insert(Brand(None, "Samsung"))
          val item = Products.insert(Product(None, brand.id.get, cat.id.get, "galaxy", "snippet", "description", "imageurl", Nil))
          val a1 = ProductAttributes.insert(ProductAttribute(None, item.id.get, "key1", "value1"))
          val a2 = ProductAttributes.insert(ProductAttribute(None, item.id.get, "key2", "value2"))

          val Some(res) = Products.findWithAttributes(item.id.get)

          res._1 must equalTo(item)
          res._2 must have length(2)
          res._2 must containAllOf(List(a1, a2))
        }
      }
    }

    "find product with no attributes" in {
      running(FakeApplication(additionalConfiguration = inMemoryDatabase())) {
        DB.withSession{ implicit s:Session =>
          val cat = Categories.insert(Category(None, "Phones"))
          val brand = Brands.insert(Brand(None, "Samsung"))
          val item = Products.insert(Product(None, brand.id.get, cat.id.get, "galaxy", "snippet", "description", "imageurl", Nil))

          val Some(res) = Products.findWithAttributes(item.id.get)

          res._1 must equalTo(item)
          res._2 must beEmpty
        }
      }
    }

    "be updated if needed" in {
      running(FakeApplication(additionalConfiguration = inMemoryDatabase())) {
        DB.withSession { implicit s: Session =>
          val c = Categories.insert(Category(None, "Phone", Some("description")))

          Categories.update(c.id.get, Category(name = "Phones"))

          val Some(cat) = Categories.findById(c.id.get)
          cat.name must equalTo("Phones")
        }
      }
    }
  }
  
}