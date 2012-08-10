package no.simplicityworks.kitchenlogistics

import org.specs2.mutable._
import cc.spray._
import test._
import http._
import HttpMethods._
import StatusCodes._
import org.scalaquery.session._
import org.scalaquery.ql.basic.BasicDriver.Implicit._

class StorageServiceSpec extends Specification with SprayTest with StorageService {

  ProductDb.database withSession { session: Session =>
    implicit val s = session
    ProductDb.Products insertAll(
      (1, "Hei"),
      (2, "Yo")
      )
  }

  "The StorageService" should {
    "return a list of products for GET requests on existing products" in {
      testService(HttpRequest(GET, "/rest/products")) {
        storageService
      }.response.content.as[String] mustEqual Right("Say hello to Spray!")
    }

    "return saved product with product id for accepted PUT requests" in {
      testService(HttpRequest(POST, "/rest/products?code=271")) {
        storageService
      }.response mustEqual HttpResponse(MethodNotAllowed, "HTTP method not allowed, supported methods: GET")
    }
  }

}