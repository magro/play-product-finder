package models

case class Phone(age: Int, id: String, name: String, snippet: String, imageUrl: String)
case class Category(id: Option[Int] = None, name: String, description: Option[String] = None)
case class Brand(id: Option[Int] = None, name: String, description: Option[String] = None)

case class Product(
    id: Option[Long], brandId: Int, categoryId: Int,
    name: String, snippet: String, description: String,
    imageUrl: String, imageUrls: List[String]) {
//  def category(implicit s: Session): Category = Categories.findById(categoryId).get
//  def brand(implicit s: Session): Brand = Brands.findById(brandId).get
}

case class ProductAttribute(id: Option[Long] = None, productId: Long, name: String, value: String)