package no.simplicityworks.kitchenlogistics

import android.content.Context

/**
 * @author Thor Åge Eldby (thoraageeldby@gmail.com)
 */

trait Storage {

  val database: Database

  trait Database {
    def findByIdentifier(identifier: String): Option[Product]
    def save(product: Product): Product
  }

  case class Product(id: Option[Long], identifier: String, name: String)

}