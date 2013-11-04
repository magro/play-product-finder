package controllers

import org.specs2.mutable._
import org.specs2.runner._
import org.junit.runner._
import play.api.test._
import play.api.test.Helpers._
import net.fwbrasil.activate.test._

@RunWith(classOf[JUnitRunner])
class ShopsControllerSpec extends Specification with ActivateTest {

  override def strategy: Strategy = recreateDatabaseStrategy
  override def context(app: play.api.Application) = models.shopPersistenceContext

  private def AuthenticatedFakeRequest() = FakeRequest().withSession(ShopsSecurity.Username -> "foo@bar.org")

  "ShopsController" should {

    "redirect to login for unauthenticated /shops request" inActivate {

      val result = controllers.ShopsController.list(0, 2, "")(FakeRequest())

      status(result) must equalTo(SEE_OTHER)
      redirectLocation(result) must beSome.which(_ == routes.ShopsController.login.url)

    }

    "list shops on the first page" inActivate {

      val result = controllers.ShopsController.list(0, 2, "")(AuthenticatedFakeRequest())

      status(result) must equalTo(OK)
      contentAsString(result) must contain("3 shops found")

    }
    
    "filter shop by name" inActivate {

      val result = controllers.ShopsController.list(0, 2, "Pauli")(AuthenticatedFakeRequest())

      status(result) must equalTo(OK)
      contentAsString(result) must contain("2 shops found")

    }
    
    "create new shop" inActivate {

      val badResult = controllers.ShopsController.save(AuthenticatedFakeRequest())
      status(badResult) must equalTo(BAD_REQUEST)

      val badCharset = controllers.ShopsController.save(
        AuthenticatedFakeRequest().withFormUrlEncodedBody("name" -> "FooBar", "queryUrlEncoding" -> "bad")
      )
      status(badCharset) must equalTo(BAD_REQUEST)
      contentAsString(badCharset) must contain("""<input type="text" id="name" name="name" value="FooBar" """)
      contentAsString(badCharset) must contain("""<input type="text" id="queryUrlEncoding" name="queryUrlEncoding" value="bad" """)

      val missingFields = controllers.ShopsController.save(
        AuthenticatedFakeRequest().withFormUrlEncodedBody("name" -> "FooBar")
      )
      status(missingFields) must equalTo(BAD_REQUEST)
      contentAsString(missingFields) must contain("""<input type="text" id="name" name="name" value="FooBar" """)

      val result = controllers.ShopsController.save(
        AuthenticatedFakeRequest().withFormUrlEncodedBody(
          "name" -> "FooBar",
          "url" -> "url",
          "active" -> "false",
          "queryUrlTemplate" -> "queryUrlTemplate",
          "queryUrlEncoding" -> "ISO-8859-15",
          "responseEncoding" -> "",
          "imageUrlBase" -> "imageUrlBase",
          "itemXPath" -> "itemXPath",
          "nameXPath" -> "nameXPath",
          "priceXPath" -> "priceXPath",
          "imageUrlXPath" -> "imageUrlXPath",
          "detailsUrlXPath" -> "detailsUrlXPath"))

      status(result) must equalTo(SEE_OTHER)
      redirectLocation(result) must beSome.which(_ == routes.ShopsController.list().url)
      flash(result).get("success") must beSome.which(_ == "Shop FooBar has been created")

      val list = controllers.ShopsController.list(0, 2, "FooBar")(AuthenticatedFakeRequest())

      status(list) must equalTo(OK)
      contentAsString(list) must contain("One shop found")

    }
    
  }
  
}