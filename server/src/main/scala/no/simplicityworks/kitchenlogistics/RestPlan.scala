package no.simplicityworks.kitchenlogistics

import unfiltered.filter.Plan
import unfiltered.request._
import unfiltered.response._
import unfiltered.directives._
import unfiltered.directives.Directives._
import org.json4s.native.Serialization
import org.json4s.native.Serialization.write
import org.json4s.NoTypeHints
import scala.util.Random
import no.simplicityworks.kitchenlogistics.DatabaseModule.{Products, Product}
import scala.Product
import scala.slick.lifted.Query

object RestPlan extends Plan {

  implicit val formats = Serialization.formats(NoTypeHints)

  def contentType(tpe: String) =
    when {
      case RequestContentType(`tpe`) =>
    } orElse UnsupportedMediaType ~> ResponseString("Content type supported: " + tpe)

  def intent = Directive.Intent.Path {
    case Seg("rest" :: "product" :: Nil) =>
      (for {
        method <- GET
        _ <- Accepts.Json
        code <- extract
        r <- request[Any]
      } yield Ok ~> ResponseString(write(Products.findByCode(code)))
        ).orElse(
        for {
          _ <- PUT
          _ <- Accepts.Json
          r <- request[Any]
        } yield Ok ~> ResponseString(Body string r))
  }


  def extract: Params.Extract[Nothing, String] = {
    new Params.Extract("code", Params.first ~> Params.nonempty)
  }
}
