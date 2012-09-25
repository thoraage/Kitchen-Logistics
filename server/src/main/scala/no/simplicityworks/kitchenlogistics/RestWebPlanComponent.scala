package no.simplicityworks.kitchenlogistics

import net.liftweb.json._
import Serialization._
import unfiltered.request._
import unfiltered.response.{Ok, NotFound, ResponseString}
import scala.Some
import unfiltered.filter.Plan
import unfiltered.jetty.Server

trait RestWebPlanComponent extends WebPlanComponent with ThreadMountedScalaQuerySessionComponent {
  this: ProductDatabaseComponent =>

  override def registerWebPlan = ((server: Server) => server.context("/rest") {
    _.filter(new RestServicePlan with ThreadMountedScalaQuerySession)
  }) :: super.registerWebPlan

  trait RestServicePlan extends Plan with ScalaQuerySession {
    val ProductsPath = "rest" :: "products" :: Nil
    val ItemsPath = "rest" :: "items" :: Nil

    object ItemsIdPath {
      def unapply(path: List[String]): Option[Int] = {
        path match {
          case "rest" :: "items" :: AsInt(id) :: Nil => Some(id)
          case _ => None
        }
      }
    }

    object AsInt {
      def unapply(value: String): Option[Int] = try {
        Some(value.toInt)
      } catch {
        case _: NumberFormatException => None
      }
    }

    override def intent = {
      case GET(Path(Seg(ProductsPath))) & Params(params) =>
        params.get("code") match {
          case Some(Seq(code)) =>
            ResponseString(write(Products.findByCode(code)))
          case _ =>
            NotFound ~> ResponseString("Missing code")
        }

      case req@PUT(Path(Seg(ProductsPath))) =>
        val product = Products.insert(read[Product](Body.string(req)))
        ResponseString(write(product))

      case req@PUT(Path(Seg(ItemsPath))) =>
        val item = Items.insert(read[Item](Body.string(req)))
        ResponseString(write(item))

      case DELETE(Path(Seg(ItemsIdPath(id)))) =>
        Items.delete(id)
        Ok

      case GET(Path(Seg(ItemsIdPath(id)))) =>
        val items = Items.findById(id)
        items.headOption.map(item => ResponseString(write(item))).getOrElse(NotFound)
    }
  }

}
