package no.simplicityworks.kitchenlogistics

import java.util.Date

/**
 * @author Thor Åge Eldby (thoraageeldby@gmail.com)
 */

trait Storage {

  val database: Database

  trait Database {
    def findProductById(id: Long): Product
    def findProductByCode(identifier: String): Option[Product]
    def saveProduct(product: Product): Product
    def saveItem(item: Item): Item
    def findItems(): Seq[ItemSummary]
  }

    case class Item(id: Option[Long], productId: Long) {
      lazy val product = database.findProductById(productId)
    }

}

case class Product(id: Option[Int], code: String, name: String, created: String)
case class ItemSummary(count: Int, product: Product, lastItemId: Int)
