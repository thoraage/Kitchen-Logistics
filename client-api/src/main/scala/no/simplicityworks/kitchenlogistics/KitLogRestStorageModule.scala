package no.simplicityworks.kitchenlogistics

import java.net.URLEncoder
import java.text.SimpleDateFormat

import scalaz._, Scalaz._
import argonaut._, Argonaut._
import no.simplicityworks.kitchenlogistics.ContentType.json

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

trait KitLogRestStorageModule extends StorageModule with StorageConfigurationModule {

    lazy val storage = new Storage {
        private val host = storageConfiguration.hostAddress
        private val http = HttpConnection(host).authenticator(storageConfiguration.authenticator)

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
            casecodec5(Product.apply, Product.unapply)("id", "code", "name", "languageIso639_2", "created")
        implicit def itemSummaryCodecJson: CodecJson[ItemSummary] =
            casecodec4(ItemSummary.apply, ItemSummary.unapply)("count", "product", "lastItemId", "itemGroupId")
        implicit def itemGroupCodecJson: CodecJson[ItemGroup] =
            casecodec4(ItemGroup.apply, ItemGroup.unapply)("id", "userId", "name", "created")
        implicit def itemCodecJson: CodecJson[Item] =
            casecodec7(Item.apply, Item.unapply)("id", "userId", "productId", "itemGroupId", "amount", "created", "updated")

        def parse[X: DecodeJson](str: String): X =
            Parse.decodeEither[X](str) match {
                case -\/(err) => sys.error(s"Trouble parsing $str: $err")
                case \/-(x) => x
            }

        override def findProductByCode(identifier: String) =
            Future(parse[List[Product]](get("/rest/products", ("code" -> identifier) :: Nil)))

        override def saveProduct(product: Product): Future[Product] = Future {
            product.id.map { id =>
                put(s"/rest/products/$id", product)
                product
            }.getOrElse(product.copy(id = put(s"/rest/products", product)))
        }

        def put[T](path: String, obj: T)(implicit code: CodecJson[T]): Option[Int] = {
            val string = http.accept(json).contentType(json).put(path, obj.asJson.toString())
            Parse.decodeOption[Id](string).map(_.id)
        }

        override def saveItem(item: Item): Future[Item] = Future {
            item.id.map { id =>
                put(s"/rest/items/$id", item)
                item
            }.getOrElse(item.copy(id = put(s"/rest/items", item)))
        }

        override def findItemsByGroup(itemGroup: Option[ItemGroup] = None) = Future {
            val queryItemGroup = itemGroup.flatMap(_.id).map("itemGroup" -> _.toString)
            parse[List[ItemSummary]](get("/rest/items", queryItemGroup.toList))
        }

        override def getRecentItems(limit: Int) = Future {
            val queryParam = List("limit" -> limit.toString, "sortBy" -> "latest")
            parse[List[ItemSummary]](get("/rest/items", queryParam))
        }

        override def searchItems(search: String): Future[Seq[ItemSummary]] = Future {
            Parse.decodeOption[Stream[ItemSummary]](get("/rest/items", List("filter" -> search))).get
        }

        def get(path: String, queryParameters: List[(String, String)] = Nil): String = {
            val query = queryParameters.map(p => s"${p._1}=${URLEncoder.encode(p._2, "UTF-8")}").mkString("?", "&", "")
            http.accept(json).get(s"$path$query")
        }

        override def getItemGroups =
            Future(parse[List[ItemGroup]](get("/rest/itemGroups")))

        override def getItemGroup(itemGroupId: Int) =
            Future(parse(get(s"/rest/itemGroups/$itemGroupId")))

        override def saveItemGroup(itemGroup: ItemGroup): Future[ItemGroup] = Future {
            itemGroup.id.map { id =>
                put(s"/rest/itemGroups/$id", itemGroup)
                itemGroup
            }.getOrElse(itemGroup.copy(id = put(s"/rest/itemGroups", itemGroup)))
        }

        override def removeItem(itemId: Int) =
            Future(http.delete(s"/rest/items/$itemId"))

        override def findItemsByCode(code: String) =
            Future(parse[List[ItemSummary]](get("/rest/items", List("code" -> code))))

        override def removeItemGroup(itemGroupId: Int): Future[Unit] =
            Future(http.delete(s"/rest/itemGroups/$itemGroupId"))

        override def getItem(itemId: Int) =
            Future(parse(get(s"/rest/items/$itemId")))

        override def getProduct(productId: Int) =
            Future(parse(get(s"/rest/products/$productId")))

        override def getItemsByProductAndGroup(productId: Int, itemGroupId: Option[Int]) = Future {
            val query = List("itemsOnly" -> "true", "product" -> productId.toString) ++ itemGroupId.map("itemGroup" -> _.toString).toList
            parse[List[Item]](get(s"/rest/items/", query))
        }
    }

}
