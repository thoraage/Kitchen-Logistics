package no.simplicityworks.kitchenlogistics

import unfiltered.filter.Plan
import unfiltered.request._
import unfiltered.response._
import unfiltered.directives._
import unfiltered.directives.Directives._
import org.json4s.native.Serialization
import org.json4s.native.Serialization.{write, read}
import org.json4s.NoTypeHints
import no.simplicityworks.kitchenlogistics.DatabaseModule._
import scala.slick.driver.H2Driver.simple._

object RestPlan extends Plan {

  implicit val formats = Serialization.formats(NoTypeHints)

  def contentType(tpe: String) =
    when {
      case RequestContentType(`tpe`) =>
    } orElse UnsupportedMediaType ~> ResponseString("Content type supported: " + tpe)

  def extract: Params.Extract[Nothing, String] =
    new Params.Extract("code", Params.first ~> Params.nonempty)

  def intent = Directive.Intent.Path {
    case Seg("rest" :: "products" :: Nil) =>
      (for {
        method <- GET
        _ <- Accepts.Json
        code <- extract
        r <- request[Any]
      } yield Ok ~> ResponseString(write(Products.findByCode(code)))
        ).orElse(
        for {
          _ <- PUT
          r <- request[Any]
        } yield {
          val id = Products.insert(read[Product](Body string r))
          Ok ~> ResponseString(write(Map("id" -> id)))
        })
    case Seg("rest" :: "products" :: "items" :: Nil) =>
      (for {
        method <- GET
        _ <- Accepts.Json
        r <- request[Any]
      } yield {
        val items = (database withSession { implicit session: Session =>
          (for {
            item <- Items
            product <- item.product
          } yield (product, item))
            .groupBy(p => (p._1.id, p._1.name))
            .map { case (id, pair) => (id, pair.length, pair.map(_._2.id).max) }.list
        }).map(p => Map("count" -> p._2, "product" -> Map("id" -> p._1._1, "name" -> p._1._2), "lastItemId" -> p._3))
        Ok ~> ResponseString(write(items))
      }).orElse(
        for {
          _ <- PUT
          r <- request[Any]
        } yield {
          val id = Items.insert(read[Item](Body string r))
          Ok ~> ResponseString(write(Map("id" -> id)))
        })
  }

}
