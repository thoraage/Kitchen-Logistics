package no.simplicityworks.kitchenlogistics

import net.liftweb.json._
import unfiltered.request._
import unfiltered.response._
import org.scalaquery.ql.{Query, SimpleFunction}
import unfiltered.filter.Plan

trait WebServiceComponent {

  unfiltered.jetty.Http(8080).context("/") {
    _.resources(getClass.getResource("/public"))
  }.context("/scan") {
    _.filter(new ScanServicePlan with ThreadMountedScalaQuerySession)
  }.run()

  trait ScanServicePlan extends Plan with ScalaQuerySession with ProductDatabase {
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

      case GET(Path(Seg("scan" :: "products" :: code :: Nil))) =>
        Items insertValue read[Item](Body.string(req))
        ResponseString(write(Query(Items).where(_.id === Query(identityFunction).first).list.head))
        Redirect("/public/")

    }
  }

}