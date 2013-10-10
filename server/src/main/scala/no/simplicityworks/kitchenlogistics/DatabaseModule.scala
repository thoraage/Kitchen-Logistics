package no.simplicityworks.kitchenlogistics

import com.mchange.v2.c3p0.ComboPooledDataSource
import java.sql.Date

trait DatabaseModule extends DatabaseProfileModule {

  import driver.simple._

  object Products extends Table[Product]("global_product") {
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
        forInsert returning id insert product
      }
  }

  object Items extends Table[Item]("user_item") {
    def id = column[Int]("id", O.PrimaryKey, O.AutoInc)
    def productId = column[Int]("product_id")
    def created = column[Date]("created")
    def product = foreignKey("item_product_fk", productId, Products)(_.id)
    def * = id.? ~ productId ~ created <> (Item, Item.unapply _)
    def forInsert = productId ~ created <> ({t => Item(None, t._1, t._2)}, {(i: Item) => Some((i.productId, i.created))})

    def all = database withSession { implicit session: Session =>
      Query(Items).list
    }
    def insert(item: Item): Int = database withSession { implicit session: Session =>
      forInsert returning id insert item
    }
    def delete(id: Int): Int = database withSession { implicit session: Session =>
      Query(Items).where(_.id === id).delete
    }

  }

  lazy val database = Database.forDataSource(new ComboPooledDataSource())
  database withSession { implicit session: Session =>
    Products.ddl.create
    Items.ddl.create
    Products.insertAll(
      Product(None, "5423", "Nexus S"),
      Product(None, "43123", "Motorola XOOM™ with Wi-Fi"),
      Product(None, "43728432", "ROLA XOOM™"))
  }

}

