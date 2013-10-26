package controllers

import models.ProductInfo
import scala.util.Sorting
import scala.collection.immutable.TreeMap
import scala.util.Sorting
import play.api.i18n.Messages
import com.sun.jna.platform.win32.Advapi32Util.Account
import play.api.mvc.QueryStringBindable

sealed trait ProductsSorting {
  val id: String
  def messageKey: String = "products.search.sorting." + id
  def sort(shopProducts: List[List[ProductInfo]]): Iterable[ProductInfo]
}

object ProductsSorting {

  private[controllers] val values = List(SortByIndex, SortByShop, SortByPriceAsc, SortByPriceDesc)

  def options: Seq[(String, String)] = values.map(s => (s.id, Messages(s.messageKey)))

  def byId(id: String): Option[ProductsSorting] = values.find(_.id == id)

  /**
   * Support binding the
   */
  implicit def bindableProductsSorting(implicit stringBinder: QueryStringBindable[String]) = new QueryStringBindable[ProductsSorting] {

    val defaultSorting = SortByIndex

    override def bind(key: String, params: Map[String, Seq[String]]): Option[Either[String, ProductsSorting]] = {
      params.get(key).getOrElse(List(defaultSorting.id)).headOption.map { value =>
        byId(value) match {
          case Some(item) => Right(item)
          case None => Left("Unknown sort param '" + value + "'")
        }
      }
    }

    override def unbind(key: String, value: ProductsSorting): String = {
      if(value == defaultSorting) ""
      else key + "=" + value.id
    }

    override def javascriptUnbind: String = s"""function(k,v) {
      return v == '${defaultSorting.id}' ? "" : encodeURIComponent(k)+'='+encodeURIComponent(v);
    }"""
  }

}

case object SortByIndex extends ProductsSorting {
  val id = "index"
  def sort(shopProducts: List[List[ProductInfo]]): Iterable[ProductInfo] = {
    TreeMap(shopProducts.map(_.zipWithIndex).flatten.groupBy(_._2).toArray: _*)
      .values.flatten.map(_._1)
  }
}

case object SortByShop extends ProductsSorting {
  val id = "shop"
  def sort(shopProducts: List[List[ProductInfo]]): Iterable[ProductInfo] = {
    shopProducts.flatten
  }
}

case object SortByPriceAsc extends ProductsSorting {
  val id = "priceAsc"
  def sort(shopProducts: List[List[ProductInfo]]): Iterable[ProductInfo] = {
    shopProducts.flatten.sortWith(_.price.getAmount.doubleValue() < _.price.getAmount.doubleValue())
  }
}

case object SortByPriceDesc extends ProductsSorting {
  val id = "priceDesc"
  def sort(shopProducts: List[List[ProductInfo]]): Iterable[ProductInfo] = {
    shopProducts.flatten.sortWith(_.price.getAmount.doubleValue() > _.price.getAmount.doubleValue())
  }
}