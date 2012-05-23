package no.simplicityworks.kitchenlogistics

import org.scalaquery.session.Database.threadLocalSession
import org.scalaquery.ql.extended.ExtendedTable
import org.scalaquery.ql.extended.H2Driver.Implicit._
import org.scalaquery.ql.TypeMapper._
import java.sql.{DriverManager, Connection}
import org.scalaquery.session.{BaseSession, Database}

object PersonDb {

  val T = new ExtendedTable[(Int, String)]("TTT") {
    def id = column[Int]("ID", O.PrimaryKey)

    def name = column[String]("NAME")

    def * = id ~ name
  }

  //val database = Database.forURL("jdbc:h2:mem:test1", driver = "org.h2.Driver")

  val database = new C3P0Database("jdbc:h2:mem:test1")/*new Database {
    Class.forName("org.h2.Driver")

    protected[session] def createConnection(): Connection = DriverManager.getConnection("jdbc:h2:mem:test1", null)

    override def createSession() = new BaseSession(this) {
      override def close() {}
    }
  }*/

  database withSession {
    T.ddl.create
    T.insert(1, "Hei")
    T.insert(2, "Yo")
    println("### yes")
    (for (t <- T) yield t.name)
      .foreach(println)
    println("### lets do it")
  }

}