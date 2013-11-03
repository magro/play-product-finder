package controllers

import org.specs2.mutable._
import play.api.test._
import play.api.test.Helpers._
import org.fluentlenium.core.filter.FilterConstructor._
import net.fwbrasil.activate.test._

class ShopsIntegrationSpec extends Specification with ActivateTest {

  // reinitializeContext
  
  override def strategy: Strategy = recreateDatabaseStrategy
  override def context(app: play.api.Application) = models.shopPersistenceContext

  "Shop managment" should {

    "work from within a browser" inBrowserWithActivate { browser =>

      browser.goTo("http://localhost:3333/")

      // Login
      browser.url() must endWith(routes.ShopsController.login.url)
      browser.$("#username").text(ShopsSecurity.ValidUsername)
      browser.$("#password").text(ShopsSecurity.ValidPassword)
      browser.$("input.btn-primary").click()

      browser.url() must endWith(routes.ShopsController.list().url)
      browser.$("#user").first.getText must contain(ShopsSecurity.ValidUsername)


      browser.$("header h1").first.getText must equalTo("Play sample application — Shopping search buddy")
      browser.$("section h1").first.getText must equalTo("3 shops found")

      browser.$("#pagination li.current").first.getText must equalTo("Displaying 1 to 3 of 3")

//      browser.$("#pagination li.next a").click()
//
//      browser.$("#pagination li.current").first.getText must equalTo("Displaying 11 to 20 of 565")
      browser.$("#searchbox").text("Pauli")
      browser.$("#searchsubmit").click()

      browser.$("section h1").first.getText must equalTo("2 shops found")
      browser.$("a", withText("FC St. Pauli")).click()

      browser.$("section h1").first.getText must equalTo("Edit shop")

      browser.$("#url").text("")
      browser.$("input.btn-primary").click()

      browser.$("div.error").size must equalTo(1)
      browser.$("div.error label").first.getText must equalTo("URL")

      browser.$("#url").text("http://example.com")
      browser.$("input.btn-primary").click()

      browser.$("section h1").first.getText must equalTo("3 shops found")
      browser.$(".alert").first.getText must equalTo("Done! Shop FC St. Pauli has been updated")

      browser.$("#searchbox").text("Pauli")
      browser.$("#searchsubmit").click()

      browser.$("a", withText("FC St. Pauli")).click()
      browser.$("input.btn-danger").click()

      browser.$("section h1").first.getText must equalTo("2 shops found")
      browser.$(".alert").first.getText must equalTo("Done! Shop has been deleted")

      browser.$("#searchbox").text("Pauli")
      browser.$("#searchsubmit").click()

      browser.$("section h1").first.getText must equalTo("One shop found")

      // Logout
      browser.$("#user").click()
      browser.$("#logout").click()
      browser.url() must endWith(routes.ShopsController.login.url)

    }

  }

}