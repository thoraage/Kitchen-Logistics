package no.simplicityworks.kitchenlogistics

import net.liftweb.json._
import Serialization._
import org.scalaquery.ql.basic.BasicDriver.Implicit._
import unfiltered.request._
import unfiltered.response._
import org.scalaquery.ql.{SimpleFunction, Query}
import unfiltered.filter.Plan

trait StorageService extends Plan with ScalaQuerySession with ProductDatabase {
  implicit val formats = DefaultFormats
  val identityFunction = SimpleFunction.nullary[Int]("identity")

  val ProductsPath = "rest" :: "products" :: Nil
  val ItemsPath = "rest" :: "items" :: Nil

  override def intent = {
    case GET(Path(Seg(ProductsPath))) & Params(params) =>
      params.get("code") match {
        case Some(Seq(code)) =>
          ResponseString(write(Query(Products).where(_.code === code).list))
        case _ =>
          NotFound ~> ResponseString("Missing code")
      }

    case req@PUT(Path(Seg(ProductsPath))) =>
      Products insertValue (read[Product](Body.string(req)))
      ResponseString(write(Query(Products).where(_.id === Query(identityFunction).first).list.head))

    case req@PUT(Path(Seg(ItemsPath))) =>
      Items insertValue read[Item](Body.string(req))
      ResponseString(write(Query(Items).where(_.id === Query(identityFunction).first).list.head))
  }
}