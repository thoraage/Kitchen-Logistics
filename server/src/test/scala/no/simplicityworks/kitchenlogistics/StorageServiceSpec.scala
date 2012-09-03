package no.simplicityworks.kitchenlogistics

import org.scalaquery.session._
import org.scalaquery.ql.basic.BasicDriver.Implicit._
import org.specs.Specification
import dispatch.Http

class StorageServiceSpec extends Specification with unfiltered.spec.jetty.Served {

  ProductDb.database withSession {
    session: Session =>
      implicit val s = session
      Products insertAll(
        Product(None, "11", "Hei"),
        Product(None, "21", "Yo")
        )
  }

  def setup = {
    _.filter(new StorageService with ThreadMountedScalaQuerySession {})
  }

  "The StorageService" should {
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
      result must beMatching("""\{"id":\d+,"code":"28","name":"Hopp"\}""")
    }

    "be able to put an item on a product and get it in return with id" in {
      val result = Http(host / "rest/items" <<< """{"productId": 1}""" as_str)
      result must beMatching("""\{"id":\d+\,"productId":1\}""")
    }
  }

}