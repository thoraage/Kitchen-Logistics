package no.simplicityworks.kitchenlogistics

import net.liftweb.json._
import Serialization._
import org.scalaquery.ql.basic.BasicDriver.Implicit._
import org.scalaquery.session.Session
import org.scalaquery.ql.Query
import unfiltered.request._
import unfiltered.response._
import net.liftweb.json.JsonAST.{JArray, JValue}

trait StorageService extends unfiltered.filter.Plan {
  implicit val formats = DefaultFormats
  def intent = {
    case Path(Seg("rest" :: "products" :: value :: Nil)) =>
      val Code = """code:(.*)""".r
      value match {
        case Code(code) =>
          //Json(JArray(
          ResponseString(
            write(ProductDb.database withSession {
              session: Session =>
                implicit val s = session
                Query(Products).where(_.code === code).list
            }   )         )
      //))
      }
  }
}