package controllers

import play.api._
import play.api.mvc._
import play.api.data._
import play.api.data.Forms._
import anorm._
import views._
import models._
import play.api.libs.ws.WS
import play.api.libs.json._
import java.io.File
import java.nio.file.Paths
import play.api.libs.concurrent.Execution.Implicits.defaultContext

object PhonesController extends Controller {

  def listOld() = Action { implicit request =>
    // val phones = List(Phone(1, "foo", "foo", "snippet", "imageurl"))

    val f = Paths.get(new File(".").getAbsolutePath(), "public", "phones", "phones.json").normalize().toString()
    val text = scala.io.Source.fromFile(f).mkString
    implicit val phoneReads = Json.reads[Phone]
    // implicit val r: Reads[List[Phone]] = 
    val phones = Json.parse(text).as[List[Phone]]
    Ok(html.phoneList(phones, "orderBy", "query"))
  }

  def list(query: String) = Action.async { implicit request =>
    // val phones = List(Phone(1, "foo", "foo", "snippet", "imageurl"))
    FcspShop.search(query).map{ products =>
      Ok(html.productList(products, "orderBy", "query"))
    }
  }

  def load() = Action {
    //Json.parse(input)
    // WS.url("http://localhost)
    Ok
  }

}

object Loader extends App {
  val f = Paths.get(Loader.getClass.getResource("/").getFile(), "..", "public", "phones", "phones.json").normalize().toString()
  val text = scala.io.Source.fromFile(f).mkString
  implicit val phoneReads = Json.reads[Phone]
  // implicit val r: Reads[List[Phone]] = 
  val json = Json.parse(text).as[List[Phone]]
  println(json)

}