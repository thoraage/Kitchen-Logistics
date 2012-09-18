package no.simplicityworks.kitchenlogistics

import net.liftweb.json._
import Serialization._
import org.scalaquery.ql.basic.BasicDriver.Implicit._
import unfiltered.request._
import unfiltered.response._
import org.scalaquery.ql.{SimpleFunction, Query}
import unfiltered.filter.Plan

trait StorageServiceComponent {

  unfiltered.jetty.Http(8080).filter(new StorageService with ThreadMountedScalaQuerySession).run()

  trait StorageService extends Plan with ScalaQuerySession with ProductDatabase {
    implicit val formats = DefaultFormats
    val identityFunction = SimpleFunction.nullary[Int]("identity")

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
      case req @ GET(Path(Seg("scan" :: Nil))) =>
        val url = req.underlying.getRequestURL
        println("REQUEST URI: " + url)
        Html5(
          <h1>Hei</h1>
          <a href={"http://zxing.appspot.com/scan?ret=" + url + "/products/{CODE}"}>Scan</a>
        )

      case GET(Path(Seg("scan" :: "products" :: code))) =>
        println("CODE: " + code)
        Html5(
          <h1>Koden er</h1>
          <span>{code}</span>
        )
    }
  }

}