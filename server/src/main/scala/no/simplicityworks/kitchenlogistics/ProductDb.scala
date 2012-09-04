package no.simplicityworks.kitchenlogistics

import org.scalaquery.session.Database.threadLocalSession
import org.scalaquery.ql.extended.ExtendedTable
import org.scalaquery.ql.extended.H2Driver.Implicit._
import org.scalaquery.ql.TypeMapper._

case class Product(id: Option[Int], code: String, name: String)

case class Item(id: Option[Int], productId: Int) {
  //lazy val product = database.findProductById(productId)
}

trait ProductDatabase {

  object Products extends ExtendedTable[Product]("PRODUCT") {
    def id = column[Int]("ID", O.PrimaryKey, O.AutoInc)

    def code = column[String]("CODE")

    def name = column[String]("NAME")

    def * = id.orElse(null) ~ code ~ name <>(Product, Product.unapply _)
  }

  object Items extends ExtendedTable[Item]("ITEM") {
    def id = column[Int]("ID", O.PrimaryKey, O.AutoInc)

    def productId = column[Int]("PRODUCT_ID")

    def * = id.orElse(null) ~ productId <>(Item, Item.unapply _)
  }

  val database = new C3P0Database("jdbc:h2:mem:test1")

  database withSession {
    Products.ddl.create
    Items.ddl.create
  }

}