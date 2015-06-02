package no.simplicityworks.kitchenlogistics

import java.util.Date

import scala.concurrent.Future

trait StorageModule {

  val storage: Database

  trait Database {
    def findProductByCode(id: Long): Future[Product]
    def findProductByCode(identifier: String): Future[Seq[Product]]
    def saveProduct(product: Product): Future[Product]
    def saveItem(item: Item): Future[Item]
    def findItems(itemGroup: Option[ItemGroup] = None): Future[Seq[ItemSummary]]
    def findItemGroups(): Future[Seq[ItemGroup]]
    def saveItemGroup(itemGroup: ItemGroup): Future[ItemGroup]
  }

  case class Product(id: Option[Int], code: String, name: String, created: Date)
  case class ItemSummary(count: Int, product: Product, lastItemId: Int)
  case class ItemGroup(id: Option[Int], userId: Option[Int], name: String, created: Date)
  case class Item(id: Option[Int], userId: Option[Int], productId: Int, itemGroupId: Int, created: Date) {
    lazy val product = storage.findProductByCode(productId)
  }

}
