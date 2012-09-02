package no.simplicityworks.kitchenlogistics

import net.liftweb.json._
import Serialization._
import org.scalaquery.ql.basic.BasicDriver.Implicit._
import org.scalaquery.session.Session
import unfiltered.request._
import unfiltered.response._
import org.scalaquery.ql.{SimpleFunction, Query}

trait StorageService extends unfiltered.filter.Plan {
  implicit val formats = DefaultFormats
  val identityFunction = SimpleFunction.nullary[Int]("identity")

  val ProductsPath = "rest" :: "products" :: Nil

  override def intent = {
    case GET(Path(Seg(ProductsPath))) & Params(params) =>
      params.get("code") match {
        case Some(Seq(code)) =>
          ResponseString(
            write(ProductDb.database withSession {
              session: Session =>
                implicit val s = session
                Query(Products).where(_.code === code).list
            }))
        case _ =>
          NotFound ~> ResponseString("Missing code")
      }

    case req @ PUT(Path(Seg(ProductsPath))) =>
      ResponseString(
        write(ProductDb.database withSession {
          session: Session =>
            implicit val s = session
            Products insertValue(read[Product](Body.string(req)))
            Query(Products).where(_.id === Query(identityFunction).first).list.head
            //JInt(Query(identityFunction).first)
        })
      )

  }
}