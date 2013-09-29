package no.simplicityworks.kitchenlogistics

import scala.slick.driver.H2Driver.simple._
import scala.slick.lifted.Query
import com.mchange.v2.c3p0.ComboPooledDataSource

object DatabaseModule {

  case class Product(id: Option[Int], code: String, name: String)

  object Products extends Table[Product]("product") {
    def id = column[Int]("id", O.PrimaryKey, O.AutoInc)
    def code = column[String]("code")
    def name = column[String]("name")
    def * = id.? ~ code ~ name <> (Product, Product.unapply _)
    def findByCode(code: String) =
      database withSession { implicit session: Session =>
        Query(Products).where(_.code === code).list
      }
  }

  private val database = Database.forDataSource(new ComboPooledDataSource())
  database withSession { implicit session: Session =>
    Products.ddl.create
    Products.insertAll(
      Product(None, "5423", "Nexus S"),
      Product(None, "43123", "Motorola XOOM™ with Wi-Fi"),
      Product(None, "43728432", "ROLA XOOM™"))
  }

}