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

object RestPlan extends Plan {

  implicit val formats = Serialization.formats(NoTypeHints)

  def contentType(tpe: String) =
    when {
      case RequestContentType(`tpe`) =>
    } orElse UnsupportedMediaType ~> ResponseString("Content type supported: " + tpe)

  def intent = Directive.Intent.Path {
    case Seg("rest" :: Nil) =>
      GET.flatMap(_ => Accepts.Json.flatMap(_ => request[Any].map(r => Ok ~> ResponseString("Hei"))))
        .orElse(PUT.flatMap(_ => request[Any].map(r => Ok ~> ResponseString("Hei"))))

    case Seg("rest" :: "product" :: Nil) =>
      (for {
        _ <- GET
        _ <- Accepts.Json
        r <- request[Any]
      } yield Ok ~> ResponseString(write(
          Array(Product("5423", "Nexus S") ::
            Product("43123", "Motorola XOOM™ with Wi-Fi") ::
            Product("43728432", "ROLA XOOM™") :: Nil,
            Product("3748", "Dull") :: Nil,
            Nil).apply(Random.nextInt(3)))))
        .orElse(
        for {
          _ <- PUT
          _ <- Accepts.Json
          r <- request[Any]
        } yield Ok ~> ResponseString(Body string r))
  }

}
