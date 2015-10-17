package no.simplicityworks.kitchenlogistics

import java.util.Date

import org.eclipse.jetty.util.resource.Resource
import org.scalatest.{Suite, BeforeAndAfterAll}

import scala.util.Properties

trait KitLogSpecBase extends BeforeAndAfterAll { this: Suite =>

    val port = 58008
    Properties.setProp("PORT", port.toString)

    val stack = new RestPlanModule with InMemoryDatabaseModule
    val http = unfiltered.jetty.Http(port)
    http.current.setBaseResource(Resource.newResource(getClass.getResource("/public").toExternalForm, false))
    stack.plans.foldLeft(http)((http, plan) => http.plan(plan))
    http.start()

    override def afterAll() {
        http.stop()
    }

    def createClient(username: String) = new KitLogRestStorageModule {
        override lazy val storageConfiguration = new StorageConfiguration {
            override lazy val hostAddress = s"http://127.0.0.1:$port"
            override lazy val userPass = (username, "pass")
        }
    }

    val client = createClient("thoredge")
    stack.database withSession { implicit session =>
        stack.Users.insert(new User(None, "notthoredge", "", stack.Users.saltPassword("pass"), new Date))
    }
    val otherClient = createClient("notthoredge")

}
