import org.specs2.mutable._
import org.specs2.runner._
import org.junit.runner._
import play.api.test._
import play.api.test.Helpers._
import net.fwbrasil.activate.test._

@RunWith(classOf[JUnitRunner])
class ShopsControllerSpec extends Specification with ActivateTest {

  override def strategy: Strategy = recreateDatabaseStrategy
  override def context(app: play.api.Application) = models.activate.shopPersistenceContext

  "ShopsController" should {
    
    "redirect to the shop list on /" inActivate {

      val result = controllers.ShopsController.index(FakeRequest())

      status(result) must equalTo(SEE_OTHER)
      redirectLocation(result) must beSome.which(_ == "/shops")

    }

    "list shops on the the first page" inActivate {

      val result = controllers.ShopsController.list(0, 2, "")(FakeRequest())

      status(result) must equalTo(OK)
      contentAsString(result) must contain("3 shops found")

    }
    
    "filter shop by name" inActivate {

      val result = controllers.ShopsController.list(0, 2, "Pauli")(FakeRequest())

      status(result) must equalTo(OK)
      contentAsString(result) must contain("2 shops found")

    }
    
    "create new shop" inActivate {

      val badResult = controllers.ShopsController.save(FakeRequest())
      status(badResult) must equalTo(BAD_REQUEST)

      val missingFields = controllers.ShopsController.save(
        FakeRequest().withFormUrlEncodedBody("name" -> "FooBar")
      )
      status(missingFields) must equalTo(BAD_REQUEST)
      contentAsString(missingFields) must contain("""<input type="text" id="name" name="name" value="FooBar" >""")

      val result = controllers.ShopsController.save(
        FakeRequest().withFormUrlEncodedBody(
          "name" -> "FooBar",
          "url" -> "url",
          "active" -> "false",
          "queryUrlTemplate" -> "queryUrlTemplate",
          "imageUrlBase" -> "imageUrlBase",
          "itemXPath" -> "itemXPath",
          "nameXPath" -> "nameXPath",
          "priceXPath" -> "priceXPath",
          "imageUrlXPath" -> "imageUrlXPath",
          "detailsUrlXPath" -> "detailsUrlXPath"))

      status(result) must equalTo(SEE_OTHER)
      redirectLocation(result) must beSome.which(_ == "/shops")
      flash(result).get("success") must beSome.which(_ == "Shop FooBar has been created")

      val list = controllers.ShopsController.list(0, 2, "FooBar")(FakeRequest())

      status(list) must equalTo(OK)
      contentAsString(list) must contain("One shop found")

    }
    
  }
  
}