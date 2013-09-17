package no.simplicityworks.kitchenlogistics

import unfiltered.filter.Plan
import unfiltered.request._
import unfiltered.response._
import unfiltered.directives._
import unfiltered.directives.Directives._
import org.json4s.native.Serialization
import org.json4s.native.Serialization.write
import org.json4s.NoTypeHints

object RestPlan extends Plan {

  implicit val formats = Serialization.formats(NoTypeHints)

  def contentType(tpe: String) =
    when {
      case RequestContentType(`tpe`) =>
    } orElse UnsupportedMediaType ~> ResponseString("Content type supported: " + tpe)

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
      } yield Ok ~> ResponseString(write(
        Product("5423", "Nexus S") ::
          Product("43123", "Motorola XOOM™ with Wi-Fi") ::
          Product("43728432", "ROLA XOOM™") :: Nil))
  }

}
