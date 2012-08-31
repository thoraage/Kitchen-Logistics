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
        Product(Some(1), "11", "Hei"),
        Product(Some(2), "21", "Yo")
        )
  }

  def setup = {
    _.filter(new StorageService {})
  }

  "The StorageService" should {
    "return empty list of products for non-matching code" in {
      Http(host / "rest/products/code:78" as_str) must_== "[]"
    }

    "return single item list of products for matching code" in {
      Http(host / "rest/products/code:11" as_str) must_== """[{"id":1,"code":"11","name":"Hei"}]"""
    }
  }

}