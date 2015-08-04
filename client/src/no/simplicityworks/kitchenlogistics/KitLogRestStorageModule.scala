package no.simplicityworks.kitchenlogistics

import java.net.URLEncoder
import java.text.SimpleDateFormat

import argonaut.Argonaut._
import argonaut._
import org.apache.http.HttpResponse
import org.apache.http.auth.{AuthScope, UsernamePasswordCredentials}
import org.apache.http.client.methods.{HttpDelete, HttpGet, HttpPut}
import org.apache.http.entity.StringEntity
import org.apache.http.impl.client.DefaultHttpClient

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.io.Source
import scala.language.postfixOps

trait KitLogRestStorageModule extends StorageModule {

    val storage = new Storage {
        val host =
//            "http://192.168.168.120:8080"
//            "http://192.168.42.47:8080"
//            "http://192.168.1.206:8080"
            "http://192.168.0.198:8080"
//            "http://192.168.0.100:8080"
//            "http://192.168.2.197:8080"
//            "http://localhost:8080"
//            "http://10.20.11.167:8080"
//            "http://kitlog.herokuapp.com"
//            "http://172.30.16.69:8080"
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

        override def findProductByCode(identifier: String): Future[Seq[Product]] = Future {
            Parse.decodeOption[Stream[Product]](get("products", ("code" -> identifier) :: Nil)).get
        }

        override def saveProduct(product: Product): Future[Product] = Future {
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

        override def saveItem(item: Item): Future[Item] = Future {
            item.copy(id = put(s"$host/rest/items", item))
        }

        override def findItemsByGroup(itemGroup: Option[ItemGroup] = None): Future[Seq[ItemSummary]] = Future {
            val queryItemGroup = itemGroup.flatMap(_.id).map("itemGroup" -> _.toString)
            Parse.decodeOption[Stream[ItemSummary]](get("items", queryItemGroup.toList)).get
        }

        def get(resource: String, queryParameters: List[(String, String)] = Nil): String = {
            val query = queryParameters.map(p => s"${p._1}=${URLEncoder.encode(p._2, "UTF-8")}").mkString("?", "&", "")
            val request = new HttpGet(s"$host/rest/$resource$query")
            request.setHeader("Accept", "application/json")
            val response = client.execute(request)
            assert2xxResponse(response)
            Source.fromInputStream(response.getEntity.getContent).mkString
        }

        override def findItemGroups(): Future[Seq[ItemGroup]] = Future {
            Parse.decodeOption[Stream[ItemGroup]](get("itemGroups")).get
        }

        override def saveItemGroup(itemGroup: ItemGroup): Future[ItemGroup] = Future {
            itemGroup.copy(id = put(s"$host/rest/itemGroups", itemGroup))
        }

        override def removeItem(itemId: Int): Future[Unit] =
            Future(client.execute(new HttpDelete(s"$host/rest/items/$itemId")))

        override def findItemsByCode(code: String): Future[Seq[ItemSummary]] = Future {
            Parse.decodeOption[Stream[ItemSummary]](get("items", List("code" -> code))).get
        }
    }

    def assert2xxResponse(response: HttpResponse): Unit = {
        if (response.getStatusLine.getStatusCode / 100 != 2) {
            throw new scala.RuntimeException(s"Invalid http status ${response.getStatusLine}")
        }
    }
}
