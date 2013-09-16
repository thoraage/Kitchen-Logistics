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
        _ <- Accepts.Json
        r <- request[Any]
      } yield Ok ~> ResponseString(
        """
          |[
          |        {"name": "Nexus S",
          |         "code": "5423"},
          |        {"name": "Motorola XOOM™ with Wi-Fi",
          |         "code": "43123"},
          |        {"name": "MOTOROLA XOOM™",
          |         "code": "43728432"}
          |]
        """.stripMargin)
  }

}
