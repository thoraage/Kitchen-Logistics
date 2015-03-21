package no.simplicityworks.kitchenlogistics

import org.apache.http.auth.{AuthScope, UsernamePasswordCredentials}
import org.apache.http.client.methods.HttpGet
import org.apache.http.impl.client.DefaultHttpClient

import argonaut.Argonaut._
import argonaut._

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

        override def findProductByCode(identifier: String): Option[Product] = ???

        override def saveProduct(product: Product): Product = ???

        override def saveItem(item: Item): Item = ???

        override def findItems(): Seq[ItemSummary] = {
            Seq(ItemSummary(8, Product(None, "", "", ""), 8))
            val request = new HttpGet(s"$host/rest/items")
            request.setHeader("Accept", "application/json")
            val response = client.execute(request)
            if (response.getStatusLine.getStatusCode / 100 != 2) {
                throw new RuntimeException(s"Invalid http status ${response.getStatusLine}")
            }
            val string = Source.fromInputStream(response.getEntity.getContent).mkString
            Parse.decodeOption[Stream[ItemSummary]](string).get
        }

        override def findProductByCode(code: Long): Product = ???

    }

}
