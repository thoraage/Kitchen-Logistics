package no.simplicityworks.kitchenlogistics

import android.content.Context

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
    def findItems(): Seq[Item]
  }

  case class Product(id: Option[Long], code: String, name: String)
  case class Item(id: Option[Long], productId: Long) {
    lazy val product = database.findProductById(productId)
  }

}