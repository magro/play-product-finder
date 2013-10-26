package controllers

import org.specs2.mutable._
import org.specs2.runner._
import org.junit.runner._
import play.api.test._
import play.api.test.Helpers._
import models.ProductInfo
import org.joda.money.Money
import org.joda.money.CurrencyUnit
import play.api.i18n.Messages

@RunWith(classOf[JUnitRunner])
class ProductsSortingSpec extends Specification {

  private val p1 = ProductInfo("p1", Money.of(CurrencyUnit.EUR, 10), "imageUrl", "detailsUrl", "shop1")
  private val p2 = ProductInfo("p2", Money.of(CurrencyUnit.EUR, 8), "imageUrl", "detailsUrl", "shop1")
  private val p3 = ProductInfo("p3", Money.of(CurrencyUnit.EUR, 6), "imageUrl", "detailsUrl", "shop2")
  private val p4 = ProductInfo("p4", Money.of(CurrencyUnit.EUR, 4), "imageUrl", "detailsUrl", "shop3")
  private val p5 = ProductInfo("p5", Money.of(CurrencyUnit.EUR, 2), "imageUrl", "detailsUrl", "shop3")
  private val shopProducts = List(List(p1, p2), List(p3), List(p4, p5))

  "SortByIndex" should {
    "sort products by index" in {
      SortByIndex.sort(shopProducts) must equalTo(List(p1, p3, p4, p2, p5))
    }
  }

  "SortByShop" should {
    "sort products by shop" in {
      SortByShop.sort(shopProducts) must equalTo(List(p1, p2, p3, p4, p5))
    }
  }

  "SortByPriceAsc" should {
    "sort products by price ascending" in {
      SortByPriceAsc.sort(shopProducts) must equalTo(List(p5, p4, p3, p2, p1))
    }
  }

  "SortByPriceDesc" should {
    "sort products by price descending" in {
      SortByPriceDesc.sort(shopProducts) must equalTo(List(p1, p2, p3, p4, p5))
    }
  }

  "ProductsSorting" should {
    "list options with id and message key" in {
      running(FakeApplication()) {
        val options = ProductsSorting.options
        options should haveSize(4)
        options should containAllOf(ProductsSorting.values.map(v => (v.id, Messages(v.messageKey))))
      }
    }
  }

  "ProductsSorting" should {
    "resolve item by id" in {
      ProductsSorting.byId(SortByIndex.id) must beSome(SortByIndex)
    }
  }

}