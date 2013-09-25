package models.slick

import play.api.db.slick.Config.driver.simple._
import play.api.Play.current

import slick.lifted.{Join, MappedTypeMapper, Projection, Query}

import _root_.models._

/*
 * Docs
 * http://slick.typesafe.com/doc/1.0.1/lifted-embedding.html#inserting
 * https://github.com/slick/slick-examples/
 * https://github.com/freekh/play-slick/blob/master/samples/computer-database/app/models/Models.scala
 * https://github.com/lunatech-labs/play-slick-examples/tree/master/app/models
 * https://github.com/playforscala/sample-applications/blob/master/ch05-storing-data-the-persistence-layer/slick-crud/app/models/Product.scala
 * 
 */
object Categories extends Table[Category]("categories") {
  def id = column[Int]("id", O.PrimaryKey, O.AutoInc)
  def name = column[String]("name")
  def description = column[String]("description", O.Nullable)
  def * = id.? ~ name ~ description.? <> (Category, Category.unapply _)
  def autoInc = * returning id
//  def forInsert = name ~ description <> ({ t => Category(None, t._1, t._2)}, { (c: Category) => Some((c.name, c.description))})
//  def insert(category: Category)(implicit session: Session): Category = {
//    val categoryId = Categories.forInsert.returning(id).insert(category)
//    Category(Some(categoryId), category.name, category.description)
//  }
  
  val byId = createFinderBy(_.id)
  
  def findById(id: Int)(implicit s:Session): Option[Category] = byId(id).firstOption
  
  def insert(category: Category)(implicit s:Session): Category = {
    val newId = autoInc.insert(category)
    category.copy(id = Some(newId))
  }

  def update(id: Int, item: Category)(implicit s:Session) {
    val itemToUpdate: Category = item.copy(Some(id))
    Categories.where(_.id === id).update(itemToUpdate)
  }

  def options(implicit s: Session): Seq[(String, String)] = {
    val query = (for {
      item <- Categories
    } yield (item.id, item.name)).sortBy(_._2)
    query.list.map(row => (row._1.toString, row._2))
  }
}

object Brands extends Table[Brand]("brands") {
  def id = column[Int]("id", O.PrimaryKey, O.AutoInc)
  def name = column[String]("name")
  def description = column[String]("description", O.Nullable)
  def * = id.? ~ name ~ description.? <> (Brand, Brand.unapply _)
  def autoInc = * returning id
  
  val byId = createFinderBy(_.id)
  def findById(id: Int)(implicit s:Session): Option[Brand] = byId(id).firstOption
  
  
  def insert(brand: Brand)(implicit s:Session): Brand = {
    val newId = autoInc.insert(brand)
    brand.copy(id = Some(newId))
  }
  def options(implicit s: Session): Seq[(String, String)] = {
    val query = (for {
      item <- Brands
    } yield (item.id, item.name)).sortBy(_._2)
    query.list.map(row => (row._1.toString, row._2))
  }
}

//class Brands extends Table[(Option[Int], String)]("brands") {
//  def id = column[Option[Int]]("id", O.PrimaryKey, O.AutoInc)
//  def name = column[String]("name", O.NotNull)
//  def * = id ~ name
//  val autoInc = name returning id into { case (name, id) => Brand(id, name) }
//  def insert(brand: Brand)(implicit session: Session): Brand = {
//    autoInc.insert(brand.name)
//  }
//}


object ProductAttributes extends Table[ProductAttribute]("product_attributes") {
  def id = column[Long]("id", O.PrimaryKey, O.AutoInc)
  def productId = column[Long]("product_id")
  def name = column[String]("name")
  def value = column[String]("value")
  def product = foreignKey("product_fk", productId, Products)(_.id)
  def * = id.? ~ productId ~ name ~ value <> (ProductAttribute, ProductAttribute.unapply _)
  def autoInc = * returning id
  
  def insert(item: ProductAttribute)(implicit s:Session): ProductAttribute = {
    val newId = autoInc.insert(item)
    item.copy(id = Some(newId))
  }
}

object Products extends Table[Product]("products") {
  def id = column[Long]("id", O.PrimaryKey, O.AutoInc)
  def brandId = column[Int]("brand_id")
  def categoryId = column[Int]("category_id")
  def name = column[String]("name")
  def snippet = column[String]("snippet")
  def description = column[String]("description")
  def imageUrl = column[String]("image_url")
  def imageUrls = column[String]("image_urls", O.Nullable)
  
  def brand = foreignKey("brand_fk", brandId, Brands)(_.id)
  def category = foreignKey("category_fk", categoryId, Categories)(_.id)
  
  def fromProduct(
      item: Product): Option[(Option[Long], Int, Int, String, String, String, String, 
	 Option[String])] = {
    val imageUrls = if(item.imageUrls.isEmpty) None else Some(item.imageUrls.mkString(","))
    Some((item.id, item.brandId, item.categoryId, item.name, item.snippet, item.description, item.imageUrl, imageUrls))
  }
  def newProduct(
      id: Option[Long], brandId: Int, categoryId: Int,
      name: String, snippet: String, description: String,
      imageUrl: String, imageUrls: Option[String]): Product = {
    Product(id, brandId, categoryId, name, snippet, description, imageUrl, imageUrls.map(urls => urls.split(",").toList).getOrElse(Nil))
  }
  
  def * = id.? ~ brandId ~ categoryId ~ name ~ snippet ~ description ~ imageUrl ~ imageUrls.? <> (newProduct _, fromProduct _)
  def autoInc = * returning id
  
  val byId = createFinderBy(_.id)
  
  def findById(id: Long)(implicit s:Session): Option[Product] = byId (id).firstOption
  
  def findWithAttributes(id: Long)(implicit s:Session): Option[(Product, List[ProductAttribute])] = {
    
    def pa = for {
	    id <- Parameters[Long]
        (p, a) <- Products leftJoin ProductAttributes on (_.id === _.productId)
        if p.id === id
    /*
	} yield (p, a)
	pa.list(id)
	.groupBy{ case (p, a) => p }
	.mapValues(_.map(_._2))
	.headOption
	*/
	} yield (p, a.id.?, a.name.?, a.value.?)
	
	val emptyMap = Map.empty[Product, List[ProductAttribute]].withDefaultValue(Nil)
	// Executing query
	val map = pa.foldLeft(id, emptyMap){ (res, row) => 
	  // The attribute id (row._2) is None if there's no attribute for the product
	  row._2.map { aId =>
	    
	    val a = ProductAttribute(Some(aId), row._1.id.get, row._3.get, row._4.get)
	    res.updated(row._1, a :: res(row._1))
	    
	  }.getOrElse(res.updated(row._1, Nil))
    }
	// We know we have only one product in the map
	map.headOption
	
	/*
	val map = pa.build(id).groupBy(_._1).mapValues { v =>
	  v.foldLeft(List.empty[ProductAttribute]){ (l, entry) =>
	    println("=============== Have entry: \n" + entry)
	    entry._2.map { aId =>
	      ProductAttribute(Some(aId), entry._1.id.get, entry._3.get, entry._4.get) :: l
	    }.getOrElse(l)
	  }
	}
	map.headOption
	*/
  }
  
  def count(filter: String)(implicit s:Session): Int =
      Query(Products.where(_.name.toLowerCase like filter.toLowerCase).length).first
  
//  def list(page: Int = 0, pageSize: Int = 10, orderBy: Int = 1, filter: String = "%")(implicit s:Session): Page[(Product, List[ProductAttribute])] = {
//
//    val offset = pageSize * page
//    
//    def attributes = for {
//	    id <- Parameters[Long]
//	    a <- ProductAttributes if a.productId === id
//	    p <- a.product
//	} yield (p, a)
//	val emptyMap = Map.empty[Product, List[ProductAttribute]].withDefaultValue(Nil)
//	val r = attributes.foldLeft(1, emptyMap)( (res, entry) => res.updated(entry._1, entry._2 :: res(entry._1)) )
//  
//    val query =
//      (for {
////        p <- Products if p.name.toLowerCase like filter.toLowerCase()
////        a <- ProductAttributes if a.productId === p.id
//        (product, att) <- Products leftJoin ProductAttributes on (_.id === _.productId)
//        if product.name.toLowerCase like filter.toLowerCase()
//      }
//      //yield (product, att.id.?, att.name.?, att.value.?)).groupBy(_._1)
//      yield (product, att))// .groupBy(_._1)
//        .drop(offset)
//        .take(pageSize)
//
//    val totalRows = count(filter)
//    // val result = query.list.map(row => (row._1, row._2.map(value => Company(Option(value), row._3.get))))
//    // val result = query.list.map(row => (row._1, row._2.map(id => id._1 ProductAttribute(Some(id), row._1))))
//    // val result = query.list.map(row => (row._1, row._2)).groupBy(_._1)
//    
//    val grouped = query.groupBy { case (product, attribute) => product }
//	val aggregated = grouped.map { case (category, query) => (category, query.length) }
//    aggregated.list
//
//    Page(result, page, offset, totalRows)
//  }
  
  def insert(product: Product)(implicit s: Session): Product = {
    val newId = autoInc.insert(product)
    product.copy(id = Some(newId))
  }
}
