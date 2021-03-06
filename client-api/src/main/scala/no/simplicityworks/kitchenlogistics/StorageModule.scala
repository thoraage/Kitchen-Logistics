package no.simplicityworks.kitchenlogistics

import java.util.Date

import scala.concurrent.Future

trait StorageModule {

    def storage: Storage

    trait Storage {
        def getItem(itemId: Int): Future[Item]

        def getProduct(productId: Int): Future[Product]

        def removeItemGroup(itemGroupId: Int): Future[Unit]

        def removeItem(itemId: Int): Future[Unit]

        def findItemsByCode(code: String): Future[Seq[ItemSummary]]

        def findProductByCode(identifier: String): Future[Seq[Product]]

        def saveProduct(product: Product): Future[Product]

        def saveItem(item: Item): Future[Item]

        def findItemsByGroup(itemGroup: Option[ItemGroup] = None): Future[Seq[ItemSummary]]

        def getRecentItems(limit: Int): Future[Seq[ItemSummary]]

        def searchItems(search: String): Future[Seq[ItemSummary]]

        def getItemGroups: Future[Seq[ItemGroup]]

        def getItemGroup(itemGroupId: Int): Future[ItemGroup]

        def saveItemGroup(itemGroup: ItemGroup): Future[ItemGroup]

        def getItemsByProductAndGroup(productId: Int, itemGroupId: Option[Int]): Future[Seq[Item]]
    }

    case class Product(id: Option[Int], code: String, name: String, languageIso639_2: String, created: Date = new Date)

    case class ItemSummary(count: Int, product: Product, lastItemId: Int, itemGroupId: Int)

    case class ItemGroup(id: Option[Int], userId: Option[Int], name: String, created: Date = new Date)

    case class Item(id: Option[Int], userId: Option[Int], productId: Int, itemGroupId: Int, amount: Float, created: Date = new Date, updated: Date = new Date)

}

trait StorageConfigurationModule {
    def storageConfiguration: StorageConfiguration
}

trait StorageConfiguration {
    def hostAddress: String
    def authenticator: Authenticator
}