package no.simplicityworks.kitchenlogistics

import cc.spray._
import org.scalaquery.ql.basic.BasicDriver.Implicit._
import org.scalaquery.session.Session


trait StorageService extends Directives {

  val storageService = {
    path("rest" / "products") {
      get {
        completeWith {
          ProductDb.database withSession { session: Session =>
              implicit val s = session
              "Say hello to Spray! => " + (for (t <- ProductDb.Products) yield t.name).list.mkString(", ")
          }
        }
      }
    }
  }

  case class Products(id: Option[Long], code: String, name: String)

  case class Item(id: Option[Long], productId: Long) {
    //lazy val product = database.findProductById(productId)
  }

}