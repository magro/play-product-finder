package models.activate

import net.fwbrasil.activate.play.EntityForm
import java.util.Date

import computerPersistenceContext._

class Company(
  var name: String)
  extends Entity

class Computer(
  var name: String,
  var introduced: Option[Date],
  var discontinued: Option[Date],
  var company: Option[Company])
  extends Entity

import models.Page

object Computer {

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

object Company {

  /**
   * Construct the Map[String,String] needed to fill a select options set.
   */
  def options: Seq[(String, String)] = transactional {
    query {
      (company: Company) =>
        where(company isNotNull) select (company.id, company.name) orderBy (company.name)
    }
  }

}

