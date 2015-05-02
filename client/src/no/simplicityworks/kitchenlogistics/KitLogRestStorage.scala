package no.simplicityworks.kitchenlogistics

import java.net.URLEncoder
import java.text.SimpleDateFormat

import argonaut.Argonaut._
import argonaut._
import org.apache.http.HttpResponse
import org.apache.http.auth.{AuthScope, UsernamePasswordCredentials}
import org.apache.http.client.methods.{HttpPut, HttpGet}
import org.apache.http.entity.StringEntity
import org.apache.http.impl.client.DefaultHttpClient

import scala.io.Source
import scala.language.postfixOps

trait KitLogRestStorage extends Storage {

    val database = new Database {
        val host =
            "http://192.168.0.198:8080"
//            "http://192.168.0.100:8080"
//            "http://192.168.2.197:8080"
//            "http://localhost:8080"
//            "http://10.20.11.167:8080"
//            "http://kitlog.herokuapp.com"
        val client = new DefaultHttpClient
        client.getCredentialsProvider.setCredentials(new AuthScope(AuthScope.ANY_HOST, AuthScope.ANY_PORT), new UsernamePasswordCredentials("thoredge", "pass"))

        // Example: 2015-03-02T00:00:00.000Z
        def format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
        implicit def DateEncodeJson: EncodeJson[java.util.Date] =
            EncodeJson(d => jString(format.format(d)))
        implicit def DateDecodeJson: DecodeJson[java.util.Date] =
            optionDecoder(_.string flatMap (s => tryTo(format.parse(s))), "java.util.Date")

        case class Id(id: Int)
        implicit def idCodeJson: CodecJson[Id] =
            casecodec1(Id.apply, Id.unapply)("id")
        implicit def productCodecJson: CodecJson[Product] =
            casecodec4(Product.apply, Product.unapply)("id", "code", "name", "created")
        implicit def itemSummaryCodecJson: CodecJson[ItemSummary] =
            casecodec3(ItemSummary.apply, ItemSummary.unapply)("count", "product", "lastItemId")
        implicit def itemGroupCodecJson: CodecJson[ItemGroup] =
            casecodec4(ItemGroup.apply, ItemGroup.unapply)("id", "userId", "name", "created")
        implicit def itemCodecJson: CodecJson[Item] =
            casecodec5(Item.apply, Item.unapply)("id", "userId", "productId", "itemGroupId", "created")

        override def findProductByCode(identifier: String): Seq[Product] = {
            Parse.decodeOption[Stream[Product]](get("products", ("code" -> identifier) :: Nil)).get
        }

        override def saveProduct(product: Product): Product = {
            product.copy(id = put(s"$host/rest/products", product))
        }

        def put[T](url: String, obj: T)(implicit code: CodecJson[T]): Option[Int] = {
            val entity = new StringEntity(obj.asJson.toString())
            val request = new HttpPut(url)
            request.setEntity(entity)
            request.setHeader("Accept", "application/json")
            val response = client.execute(request)
            assert2xxResponse(response)
            val string = Source.fromInputStream(response.getEntity.getContent).mkString
            Parse.decodeOption[Id](string).map(_.id)
        }

        override def saveItem(item: Item): Item = {
            item.copy(id = put(s"$host/rest/items", item))
        }

        override def findItems(): Seq[ItemSummary] = {
            Parse.decodeOption[Stream[ItemSummary]](get("items")).get
        }

        def get(resource: String, queryParameters: List[(String, String)] = Nil): String = {
            val query = queryParameters.map(p => s"${p._1}=${URLEncoder.encode(p._2)}").mkString("?", "&", "")
            val request = new HttpGet(s"$host/rest/$resource$query")
            request.setHeader("Accept", "application/json")
            val response = client.execute(request)
            assert2xxResponse(response)
            Source.fromInputStream(response.getEntity.getContent).mkString
        }

        override def findProductByCode(code: Long): Product = ???

        override def findItemGroups(): Seq[ItemGroup] = {
            Parse.decodeOption[Stream[ItemGroup]](get("itemGroups")).get
        }
    }

    def assert2xxResponse(response: HttpResponse): Unit = {
        if (response.getStatusLine.getStatusCode / 100 != 2) {
            throw new scala.RuntimeException(s"Invalid http status ${response.getStatusLine}")
        }
    }
}
