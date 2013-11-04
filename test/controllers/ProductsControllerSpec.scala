package controllers

import org.specs2.mutable._
import org.specs2.runner._
import org.junit.runner._
import play.api.test._
import play.api.test.Helpers._
import net.fwbrasil.activate.test._

@RunWith(classOf[JUnitRunner])
class ProductsControllerSpec extends Specification with ActivateTest {

  override def strategy: Strategy = recreateDatabaseStrategy
  override def context(app: play.api.Application) = models.shopPersistenceContext

  "ProductsController" should {
    
    "redirect to search on /" inActivate {

      val result = controllers.ProductsController.index(FakeRequest())

      status(result) must equalTo(SEE_OTHER)
      redirectLocation(result) must beSome.which(_ == routes.ProductsController.search().url)

    }
    
  }
  
}