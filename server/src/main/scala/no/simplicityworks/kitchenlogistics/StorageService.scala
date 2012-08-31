package no.simplicityworks.kitchenlogistics

import cc.spray._
import cc.spray.json._
import http.MediaTypes._
import org.scalaquery.ql.basic.BasicDriver.Implicit._
import org.scalaquery.session.Session
import org.scalaquery.ql.Query
import typeconversion.SprayJsonSupport


trait StorageService extends Directives {

  /*object ProductProtocol extends DefaultJsonProtocol {
    implicit val productFormat = jsonFormat3(Product)
  }
  import ProductProtocol._*/
  implicit val marshalling = SprayJsonSupport

  val storageService = {
    path("rest" / "products") {
      get {
        respondWithMediaType(`application/json`) {
          val value3: Product = ProductDb.database withSession {
            session: Session =>
              implicit val s = session
              Query(Products).where(_.name startsWith "").list.head
          }
           val value = Map("a" -> 1, "b" -> 2, "c" -> 3)
          _.complete {
            value
          }
        }
      }
    }
  }

}