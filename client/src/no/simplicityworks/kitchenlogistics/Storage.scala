package no.simplicityworks.kitchenlogistics

import java.util.Date

trait Storage {

  val database: Database

  trait Database {
    def findProductByCode(id: Long): Product
    def findProductByCode(identifier: String): Seq[Product]
    def saveProduct(product: Product): Product
    def saveItem(item: Item): Item
    def findItems(): Seq[ItemSummary]
    def findItemGroups(): Seq[ItemGroup]
  }

  case class Product(id: Option[Int], code: String, name: String, created: Date)
  case class ItemSummary(count: Int, product: Product, lastItemId: Int)
  case class ItemGroup(id: Option[Int], userId: Option[Int], name: String, created: Date)
  case class Item(id: Option[Int], userId: Option[Int], productId: Int, itemGroupId: Int, create: Date) {
    lazy val product = database.findProductByCode(productId)
  }

}
