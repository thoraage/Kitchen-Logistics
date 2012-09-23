package no.simplicityworks.kitchenlogistics

import org.scalaquery.session._
import org.scalaquery.ql.basic.BasicDriver.Implicit._
import org.specs.Specification
import dispatch.{StatusCode, Http}
import unfiltered.spec.jetty.Served

class RestWebPlanPlanComponentSpec extends Specification with Served {

  val plan = new RestWebPlanComponent with ProductDatabaseComponent {
    database withSession {
      session: Session =>
        implicit val s = session
        Products insertAll(
          Product(None, "11", "Hei"),
          Product(None, "21", "Yo")
          )
    }
  }

  def setup = server => plan.registerWebPlan.foldLeft(server)((s, f) => f(s))

  "The RestWebPlan" should {
    "return empty list of products for non-matching code" in {
      val result = Http(host / "rest/products" <<? Map("code" -> "78") as_str)
      result must_== "[]"
    }

    "return single product for matching code" in {
      val result = Http(host / "rest/products" <<? Map("code" -> "11") as_str)
      result must_== """[{"id":1,"code":"11","name":"Hei"}]"""
    }

    "be able to put a product and get it in return with id" in {
      val result = Http(host / "rest/products" <<< """{"code":"28","name":"Hopp"}""" as_str)
      result must beMatching( """\{"id":\d+,"code":"28","name":"Hopp"\}""")
    }

    "be able to put an item on a product and get it in return with id" in {
      val result = Http(host / "rest/items" <<< """{"productId": 1}""" as_str)
      result must beMatching( """\{"id":\d+\,"productId":1\}""")
    }

    "be able to delete an item" in {
      val Id = """.*"id" *: *(\d)*.*""".r
      Http(host / "rest/items" <<< """{"productId": 1}""" as_str) match {
        case Id(id) =>
          Http((host / ("rest/items/" + id) DELETE) as_str)
          try {
            Http(host / ("rest/items/" + id) as_str)
            fail("Not deleted")
          } catch {
            case e: StatusCode => e.code must_== 404
            case e => fail("Wrong error: " + e.toString)
          }
        case _ => fail("Id not found")
      }
    }
  }

}