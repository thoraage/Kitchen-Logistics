package no.simplicityworks.kitchenlogistics

import java.net.URLEncoder
import java.text.SimpleDateFormat

import argonaut.Argonaut._
import argonaut._
import no.simplicityworks.kitchenlogistics.ContentType.json

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

trait KitLogRestStorageModule extends StorageModule with StorageConfigurationModule {

    lazy val storage = new Storage {
        val host = storageConfiguration.hostAddress
//            "http://192.168.168.120:8080"
//            "http://192.168.42.47:8080"
//            "http://192.168.1.181:8080"
//            "http://192.168.0.195:8080"
//            "http://192.168.0.100:8080"
//            "http://localhost:8080"
//            "http://192.168.1.198:8080"
//            "http://kitlog.herokuapp.com"
//            "http://172.30.16.69:8080"
        private val connection = HttpConnection(host).basicAuth("thoredge", "pass")

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
            val string = connection.accept(json).contentType(json).put(url, obj.asJson.toString())
            Parse.decodeOption[Id](string).map(_.id)
//            val entity = new StringEntity(obj.asJson.toString())
//            val request = new HttpPut(url)
//            request.setEntity(entity)
//            request.setHeader("Accept", "application/json")
//            val response = client.execute(request)
//            assert2xxResponse(response)
//            val returnEntity = Option(response.getEntity)
//            returnEntity.flatMap { returnEntity =>
//                val string = Source.fromInputStream(returnEntity.getContent).mkString
//                Parse.decodeOption[Id](string).map(_.id)
//            }
        }

        override def saveItem(item: Item): Future[Item] = Future {
            item.id.map { id =>
                put(s"$host/rest/items/$id", item)
                item
            }.getOrElse(item.copy(id = put(s"$host/rest/items", item)))
        }

        override def findItemsByGroup(itemGroup: Option[ItemGroup] = None): Future[Seq[ItemSummary]] = Future {
            val queryItemGroup = itemGroup.flatMap(_.id).map("itemGroup" -> _.toString)
            Parse.decodeOption[Stream[ItemSummary]](get("items", queryItemGroup.toList)).get
        }

        def get(resource: String, queryParameters: List[(String, String)] = Nil): String = {
            val query = queryParameters.map(p => s"${p._1}=${URLEncoder.encode(p._2, "UTF-8")}").mkString("?", "&", "")
            connection.accept(json).get(s"/rest/$resource$query")
        }

        override def findItemGroups(): Future[Seq[ItemGroup]] = Future {
            Parse.decodeOption[Stream[ItemGroup]](get("itemGroups")).get
        }

        override def saveItemGroup(itemGroup: ItemGroup): Future[ItemGroup] = Future {
            itemGroup.id.map { id =>
                put(s"$host/rest/itemGroups/$id", itemGroup)
                itemGroup
            }.getOrElse(itemGroup.copy(id = put(s"$host/rest/itemGroups", itemGroup)))
        }

        override def removeItem(itemId: Int): Future[Unit] =
            Future(connection.delete(s"/rest/items/$itemId"))

        override def findItemsByCode(code: String): Future[Seq[ItemSummary]] = Future {
            Parse.decodeOption[Stream[ItemSummary]](get("items", List("code" -> code))).get
        }

        override def removeItemGroup(itemGroupId: Int): Future[Unit] =
            Future(connection.delete(s"/rest/itemGroups/$itemGroupId"))

        override def getItem(itemId: Int): Future[Item] = {
            Future(Parse.decodeOption[Item](get(s"items/$itemId")).getOrElse(sys.error("Unexpected Item representation received")))
        }
    }

}
