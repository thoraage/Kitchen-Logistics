package no.simplicityworks.kitchenlogistics

import com.ning.http.client.cookie.Cookie
import com.ning.http.client.{Request, AsyncHandler}
import dispatch._, Defaults._, dispatch.as._
import org.json4s._
import org.json4s.jackson.Serialization

import scala.collection.JavaConversions.asScalaIterator
import scala.concurrent.Await
import scala.concurrent.duration._
import scala.language.postfixOps
import org.json4s.DefaultJsonFormats

trait KitLogRestStorage extends Storage {

    val database = new Database {
        val http = new Http
        val host = "http://localhost:8080"
        var authCookie: Option[Cookie] = None

        def auth[T](f: => T): T = try f catch {
            case StatusCode(401) =>
                val req = url(s"$host/rest/authenticate").as_!("thoredge", "pass")
                val cookies = Await.result(for (res <- http(req)) yield res.getCookies, 5 seconds)
                authCookie = asScalaIterator(cookies.iterator()).find(_.getName == "auth")
                f
        }

        private implicit val formats = Serialization.formats(NoTypeHints)

        override def findProductByCode(identifier: String): Option[Product] = ???

        override def saveProduct(product: Product): Product = ???

        override def saveItem(item: Item): Item = ???

        override def findItems(): Seq[ItemSummary] = auth {
            val req = url(s"$host/rest/items").addCookie(authCookie.getOrElse(throw StatusCode(401))).addHeader("Accept", "application/json")
            Await.result(http(req OK json4s.Json), 5 seconds).extract[Seq[ItemSummary]]
        }

        override def findProductById(id: Long): Product = ???

    }

}
