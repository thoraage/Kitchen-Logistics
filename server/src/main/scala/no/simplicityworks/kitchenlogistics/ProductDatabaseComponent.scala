package no.simplicityworks.kitchenlogistics

import org.scalaquery.ql.extended.ExtendedTable
import org.scalaquery.ql.extended.H2Driver.Implicit._
import org.scalaquery.ql.TypeMapper._
import org.scalaquery.ql.{SimpleFunction, Query}
import org.scalaquery.session.Session
import net.liftweb.json.Serialization._
import unfiltered.request.Body

case class Product(id: Option[Int], code: String, name: String)

case class Item(id: Option[Int], productId: Int) {
  //lazy val product = database.findProductById(productId)
}

trait ProductDatabaseComponent {
  val identityFunction = SimpleFunction.nullary[Int]("identity")

  object Products extends ExtendedTable[Product]("PRODUCT") {
    def id = column[Int]("ID", O.PrimaryKey, O.AutoInc)

    def code = column[String]("CODE")

    def name = column[String]("NAME")

    def * = id.orElse(null) ~ code ~ name <>(Product, Product.unapply _)

    def findByCode(code: String)(implicit session: Session) = Query(Products).where(_.code === code).list

    def insert(product: Product)(implicit session: Session) = {
      Products insertValue product
      Query(Products).where(_.id === Query(identityFunction).first).list.head
    }
  }

  object Items extends ExtendedTable[Item]("ITEM") {
    def id = column[Int]("ID", O.PrimaryKey, O.AutoInc)

    def productId = column[Int]("PRODUCT_ID")

    def * = id.orElse(null) ~ productId <>(Item, Item.unapply _)

    def findById(id: Int)(implicit session: Session) = Query(Items).where(_.id === id).list

    def delete(id: Int)(implicit session: Session) {
      Query(Items).where(_.id === id).mutate(_.delete())
    }

    def insert(item: Item)(implicit session: Session) = {
      Items insertValue item
      Query(Items).where(_.id === Query(identityFunction).first).list.head
    }
  }

  val database = new C3P0Database("jdbc:h2:mem:test1")

  database withSession {
    import org.scalaquery.session.Database.threadLocalSession
    Products.ddl.create
    Items.ddl.create
  }

}