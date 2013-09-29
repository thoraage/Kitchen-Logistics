package no.simplicityworks.kitchenlogistics

import scala.slick.driver.H2Driver.simple._
import scala.slick.lifted.Query
import com.mchange.v2.c3p0.ComboPooledDataSource
import java.sql.Date

object DatabaseModule {

  def now = new Date(System.currentTimeMillis())

  case class Product(id: Option[Int], code: String, name: String, created: Date = now)

  case class Item(id: Option[Int], productId: Int, created: Date = now)

  object Products extends Table[Product]("product") {
    def id = column[Int]("id", O.PrimaryKey, O.AutoInc)
    def code = column[String]("code")
    def name = column[String]("name")
    def created = column[Date]("created")
    def * = id.? ~ code ~ name ~ created <> (Product, Product.unapply _)
    def forInsert = code ~ name ~ created <> ({t => Product(None, t._1, t._2, t._3)}, {(p: Product) => Some((p.code, p.name, p.created))})
    def findByCode(code: String) =
      database withSession { implicit session: Session =>
        Query(Products).where(_.code === code).list
      }
    def insert(product: Product): Int =
      database withSession { implicit session: Session =>
        Products.forInsert insert product
      }
  }

  object Items extends Table[Item]("item") {
    def id = column[Int]("id", O.PrimaryKey, O.AutoInc)
    def productId = column[Int]("product_id")
    def created = column[Date]("created")
    def * = id.? ~ productId ~ created <> (Item, Item.unapply _)
    def forInsert = productId ~ created <> ({t => Item(None, t._1, t._2)}, {(i: Item) => Some((i.productId, i.created))})
    def all = database withSession { implicit session: Session =>
      Query(Items).list
    }
    def insert(item: Item): Int = database withSession { implicit session: Session =>
      Items.forInsert insert item
    }
  }

  private val database = Database.forDataSource(new ComboPooledDataSource())
  database withSession { implicit session: Session =>
    Products.ddl.create
    Items.ddl.create
    Products.insertAll(
      Product(None, "5423", "Nexus S"),
      Product(None, "43123", "Motorola XOOM™ with Wi-Fi"),
      Product(None, "43728432", "ROLA XOOM™"))
  }

}