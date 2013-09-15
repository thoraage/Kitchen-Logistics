package no.simplicityworks.kitchenlogistics

import unfiltered.filter.Plan
import unfiltered.request._
import unfiltered.response._
import unfiltered.directives._
import unfiltered.directives.Directives._
import scala.io.Source

object RestPlan extends Plan {

  def contentType(tpe:String) =
    when{ case RequestContentType(`tpe`) => } orElse UnsupportedMediaType ~> ResponseString("Content type supported: " + tpe)

  def intent = Directive.Intent.Path {
    case Seg(Nil) =>
      for {
        _ <- GET
      } yield Redirect("/index.html")

    case Seg("rest" :: "product" :: Nil) =>
      for {
        _ <- GET
        _ <- contentType("application/json")
        r <- request[Any]
      } yield Ok ~> ResponseString("Hei")
  }

}
