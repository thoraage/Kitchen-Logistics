package no.simplicityworks.kitchenlogistics

import cc.spray._
import org.scalaquery.ql.extended.H2Driver.Implicit._
import org.scalaquery.session.Session


trait StorageService extends Directives {
  
  val storageService = {
    path("rest" / "product") {
      get { req =>
        PersonDb.database withSession { session: Session =>
          implicit val s = session
          req.complete("Say hello to Spray! => " + (for (t <- PersonDb.T) yield t.name).list.mkString(", "))
        }
      }
    }
  }

  case class Product(id: Option[Long], code: String, name: String)
  case class Item(id: Option[Long], productId: Long) {
    //lazy val product = database.findProductById(productId)
  }

}