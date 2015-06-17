package no.simplicityworks.kitchenlogistics

import java.util.Date

import scala.concurrent.Future

trait StorageModule {

    val storage: Storage

    trait Storage {
        def removeItem(itemId: Int): Future[Unit]

        def findItemsByCode(code: String): Future[Seq[ItemSummary]]

        def findProductByCode(identifier: String): Future[Seq[Product]]

        def saveProduct(product: Product): Future[Product]

        def saveItem(item: Item): Future[Item]

        def findItemsByGroup(itemGroup: Option[ItemGroup] = None): Future[Seq[ItemSummary]]

        def findItemGroups(): Future[Seq[ItemGroup]]

        def saveItemGroup(itemGroup: ItemGroup): Future[ItemGroup]
    }

    case class Product(id: Option[Int], code: String, name: String, created: Date)

    case class ItemSummary(count: Int, product: Product, lastItemId: Int)

    case class ItemGroup(id: Option[Int], userId: Option[Int], name: String, created: Date)

    case class Item(id: Option[Int], userId: Option[Int], productId: Int, itemGroupId: Int, created: Date)

}
