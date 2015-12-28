package no.simplicityworks.kitchenlogistics

import java.util.Date

import org.eclipse.jetty.util.resource.Resource
import org.scalatest.{BeforeAndAfterAll, Suite}

import scala.concurrent.duration.Duration._
import scala.concurrent.{Await, Future}
import scala.slick.driver.JdbcDriver.simple._
import scala.slick.jdbc.StaticQuery
import scala.util.{Try, Properties}

trait SpecBase extends BeforeAndAfterAll { this: Suite =>
    def await[T](future: Future[T]): T = Await.result(future, Inf)

    val port = 58008
    Properties.setProp("PORT", port.toString)

    def app: PlanCollectionModule with DatabaseModule

    lazy val http = unfiltered.jetty.Http(port)

    override protected def beforeAll() {
        http.current.setBaseResource(Resource.newResource(getClass.getResource("/public").toExternalForm, false))
        app.plans.foldLeft(http)((http, plan) => http.plan(plan))
        http.start()
        app.database withSession { implicit session =>
            app.databaseDdls.foreach(_.create)
        }
    }

    override protected def afterAll() {
        http.stop()
        http.destroy()
        http.join()
        app.database withSession { implicit session =>
            StaticQuery.updateNA("drop all objects").execute
        }
    }

    def createClient(username: String) = new KitLogRestStorageModule {
        override lazy val storageConfiguration = new StorageConfiguration {
            override lazy val hostAddress = s"http://127.0.0.1:$port"
            override lazy val authenticator = BasicAuthenticator(username, "pass")
        }
    }

    def createUser(user: User): User = {
        app.database withSession { implicit session =>
            val id = app.Users.insert(user)
            user.copy(id = Some(id))
        }
    }

    lazy val thoredge = createUser(User(None, "thoredge", "thoraageeldby@gmail.com", app.Users.saltPassword("pass"), new Date))
    lazy val client = createClient(thoredge.username)
    lazy val notThoredge = createUser(new User(None, "notthoredge", "", app.Users.saltPassword("pass"), new Date))
    lazy val otherClient = createClient(notThoredge.username)

}
