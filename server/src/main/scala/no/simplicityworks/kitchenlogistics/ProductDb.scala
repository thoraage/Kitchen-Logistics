package no.simplicityworks.kitchenlogistics

import org.scalaquery.session.Database.threadLocalSession
import org.scalaquery.ql.extended.ExtendedTable
import org.scalaquery.ql.extended.H2Driver.Implicit._
import org.scalaquery.ql.TypeMapper._
import java.sql.{DriverManager, Connection}
import org.scalaquery.session.{BaseSession, Database}


case class Product(id: Option[Int], code: String, name: String)

case class Item(id: Option[Int], productId: Long) {
  //lazy val product = database.findProductById(productId)
}

object Products extends ExtendedTable[Product]("PRODUCT") {
  def id = column[Int]("ID", O.PrimaryKey)

  def code = column[String]("CODE")

  def name = column[String]("NAME")

  def * = id.orElse(null) ~ code ~ name <> (Product, Product.unapply _)
}

object ProductDb {

  //val database = Database.forURL("jdbc:h2:mem:test1", driver = "org.h2.Driver")

  val database = new C3P0Database("jdbc:h2:mem:test1")/*new Database {
    Class.forName("org.h2.Driver")

    protected[session] def createConnection(): Connection = DriverManager.getConnection("jdbc:h2:mem:test1", null)

    override def createSession() = new BaseSession(this) {
      override def close() {}
    }
  }*/

  database withSession {
    Products.ddl.create
/*    Products insertAll(
      (1, "Hei"),
      (2, "Yo")
      )*/
  }

}