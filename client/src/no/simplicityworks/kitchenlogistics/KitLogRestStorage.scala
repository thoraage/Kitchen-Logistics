package no.simplicityworks.kitchenlogistics

import java.net.URLEncoder

import argonaut.Argonaut._
import argonaut._
import org.apache.http.auth.{AuthScope, UsernamePasswordCredentials}
import org.apache.http.client.methods.HttpGet
import org.apache.http.impl.client.DefaultHttpClient

import scala.io.Source
import scala.language.postfixOps

trait KitLogRestStorage extends Storage {

    val database = new Database {
//        val host = "http://localhost:8080"
        val host = "http://192.168.0.198:8080"
//        val host = "http://10.20.11.167:8080"
        val client = new DefaultHttpClient
        client.getCredentialsProvider.setCredentials(new AuthScope(AuthScope.ANY_HOST, AuthScope.ANY_PORT), new UsernamePasswordCredentials("thoredge", "pass"))

        implicit def productCodecJson: CodecJson[Product] =
            casecodec4(Product.apply, Product.unapply)("id", "code", "name", "created")
        implicit def itemSummaryCodecJson: CodecJson[ItemSummary] =
            casecodec3(ItemSummary.apply, ItemSummary.unapply)("count", "product", "lastItemId")

        override def findProductByCode(identifier: String): Seq[Product] = {
            Parse.decodeOption[Stream[Product]](get("products", ("code" -> identifier) :: Nil)).get
        }

        override def saveProduct(product: Product): Product = ???

        override def saveItem(item: Item): Item = ???

        override def findItems(): Seq[ItemSummary] = {
            Parse.decodeOption[Stream[ItemSummary]](get("items")).get
        }

        def get(resource: String, queryParameters: List[(String, String)] = Nil): String = {
            val query = queryParameters.map(p => s"${p._1}=${URLEncoder.encode(p._2)}").mkString("?", "&", "")
            val request = new HttpGet(s"$host/rest/$resource$query")
            request.setHeader("Accept", "application/json")
            val response = client.execute(request)
            if (response.getStatusLine.getStatusCode / 100 != 2) {
                throw new scala.RuntimeException(s"Invalid http status ${response.getStatusLine}")
            }
            Source.fromInputStream(response.getEntity.getContent).mkString
        }

        override def findProductByCode(code: Long): Product = ???

    }

}
