package models.activate

import net.fwbrasil.activate.entity.Entity
import computerPersistenceContext._

case class Phone(age: Int, id: String, name: String, snippet: String, imageUrl: String)

trait NamedEntity extends Entity {
  def name: String
}

trait Options[T <: NamedEntity] {
  def options: Seq[(String, String)] = transactional {
    query {
      (item: NamedEntity) =>
        where(item isNotNull) select (item.id, item.name) orderBy (item.name)
    }
  }
}

@Alias("category")
class Category(var name: String, var description: Option[String] = None) extends NamedEntity
object Category extends Options[Category]

@Alias("brand")
class Brand(var name: String, var description: Option[String] = None) extends NamedEntity
object Brand extends Options[Brand]

@Alias("product")
class Product(
    var brand: Brand, var category: Category,
    var name: String, var snippet: String, var description: String,
    var imageUrl: String,
    @Alias("_image_urls") var imageUrls: List[String] = Nil,
    @Alias("_attributes") var attributes: List[ProductAttribute] = Nil) extends Entity {
//  def category(implicit s: Session): Category = Categories.findById(categoryId).get
//  def brand(implicit s: Session): Brand = Brands.findById(brandId).get
}

@Alias("product_attribute")
class ProductAttribute(var name: String, var value: String) extends Entity


/*
object Product {

  def list(page: Int = 0, pageSize: Int = 10, orderBy: Int = 2, filter: String = "*"): Page[(Computer, Option[Company])] = transactional {
    val pagination =
      paginatedQuery {
        (c: Computer) =>
          where(toUpperCase(c.name) like filter.toUpperCase) select (c) orderBy {
            orderBy match {
              case -2 =>
                c.name desc
              case -3 =>
                c.introduced desc
              case -4 =>
                c.discontinued desc
              case -5 =>
                c.company.map(_.name) desc
              case 2 =>
                c.name
              case 3 =>
                c.introduced
              case 4 =>
                c.discontinued
              case 5 =>
                c.company.map(_.name)
            }
          }
      }

    val navigator = pagination.navigator(pageSize)
    if (navigator.numberOfResults > 0) {
      val p = navigator.page(page)
      Page(p.map(c => (c, c.company)), page, page * pageSize, navigator.numberOfResults)
    } else
      Page(Nil, 0, 0, 0)
  }
}
  */