package no.simplicityworks.kitchenlogistics

import org.scalaquery.ql.{SimpleFunction, Query}
import unfiltered.request._
import unfiltered.response.NotFound
import scala.Some
import unfiltered.response.ResponseString
import net.liftweb.json.DefaultFormats
import unfiltered.filter.Plan

trait RestServicePlanComponent {

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
