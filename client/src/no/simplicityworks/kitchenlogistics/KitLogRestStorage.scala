package no.simplicityworks.kitchenlogistics

import java.io.InputStreamReader

import android.util.Log

//import org.json4s._
//import org.json4s.jackson.JsonMethods._
import org.apache.http.auth.{AuthScope, UsernamePasswordCredentials}
import org.apache.http.client.methods.HttpGet
import org.apache.http.impl.client.DefaultHttpClient
//import org.json4s._
//import org.json4s.jackson.Serialization.read

import argonaut._, Argonaut._

import scala.io.Source
import scala.language.postfixOps

trait KitLogRestStorage extends Storage {

    val database = new Database {
//        val host = "http://localhost:8080"
//        val host = "http://192.168.0.198:8080"
        val host = "http://10.20.11.167:8080"
        val client = new DefaultHttpClient
        client.getCredentialsProvider.setCredentials(new AuthScope(AuthScope.ANY_HOST, AuthScope.ANY_PORT), new UsernamePasswordCredentials("thoredge", "pass"))

        implicit def productCodecJson: CodecJson[Product] =
            casecodec4(Product.apply, Product.unapply)("id", "code", "name", "created")
        implicit def itemSummaryCodecJson: CodecJson[ItemSummary] =
            casecodec3(ItemSummary.apply, ItemSummary.unapply)("count", "product", "lastItemId")

        case class Person(name: String, age: Option[Int], things: List[String])
        implicit def PersonCodecJson: CodecJson[Person] =
            casecodec3(Person.apply, Person.unapply)("name", "age", "things")

        //        implicit val formats = DefaultFormats
//        private implicit val formats = Serialization.formats(NoTypeHints)

        override def findProductByCode(identifier: String): Option[Product] = ???

        override def saveProduct(product: Product): Product = ???

        override def saveItem(item: Item): Item = ???

        override def findItems(): Seq[ItemSummary] = {
            Seq(ItemSummary(8, Product(None, "", "", ""), 8))
            val request = new HttpGet(s"$host/rest/items")
            request.setHeader("Accept", "application/json")
            val response = client.execute(request)
//            Log.e("Hei", classOf[ItemSummary].getConstructors.toList.mkString(", "))
            if (response.getStatusLine.getStatusCode / 100 != 2) {
                throw new RuntimeException(s"Invalid http status ${response.getStatusLine}")
            }
            //            read[Seq[ItemSummary]](new InputStreamReader(response.getEntity.getContent))
            val string = Source.fromInputStream(response.getEntity.getContent).mkString
            Parse.decodeOption[Stream[ItemSummary]](string).get
//            read[Seq[ItemSummary]](string)
//            parse(string) match {
//                case JArray(list) => list.map {
//                    case JObject(List((count,JInt(count)), (product,JObject(List((id,JInt(1)), (code,JString(8012156001185)), (name,JString(Tull)), (created,JString(2015-03-01T00:00:00.000Z))))), (lastItemId,JInt(3)))) =>
//                        ItemSummary(count.intValue, Product(), )
//                }
//                case _ => sys.error(s"Unable to parse $string")
//            }
        }

        override def findProductById(id: Long): Product = ???

    }

}
