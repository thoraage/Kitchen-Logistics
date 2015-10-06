package no.simplicityworks.kitchenlogistics

import java.util.Date

import org.eclipse.jetty.util.resource.Resource
import org.scalatest._

import scala.concurrent.Await
import scala.concurrent.duration.Duration.Inf
import scala.util.Properties

class ItemGroupSpec extends FeatureSpec with GivenWhenThen with BeforeAndAfterAll {

    private val port = 58008
    Properties.setProp("PORT", port.toString)

    val stack = new RestPlanModule with InMemoryDatabaseModule
    val http = unfiltered.jetty.Http(port)
    http.current.setBaseResource(Resource.newResource(getClass.getResource("/public").toExternalForm, false))
    stack.plans.foldLeft(http)((http, plan) => http.plan(plan))
    http.start()

    override def afterAll() {
        http.stop()
    }

    val client = new KitLogRestStorageModule {
        override lazy val storageConfiguration = new StorageConfiguration {
            override lazy val hostAddress = s"http://127.0.0.1:$port"
        }
    }

    def getAll = Await.result(client.storage.findItemGroups(), Inf)

    feature("Item group get all") {
        scenario("Ok") {
            assert(getAll.seq.size === 1)
        }
    }

    feature("Item group create") {
        scenario("Ok") {
            assert(getAll.seq.size === 1)
            val itemGroup = Await.result(client.storage.saveItemGroup(new client.ItemGroup(None, None, "Mine", new Date)), Inf)
            assert(itemGroup.id !== None)
            assert(getAll.seq.size === 2)
        }
    }

    feature("Item group rename") {
        scenario("Ok") {
            val itemGroup = getAll.head
            Await.result(client.storage.saveItemGroup(itemGroup.copy(name = "New name")), Inf)
            assert(getAll.filter(_.id == itemGroup.id).head.name === "New name")
        }
    }

    feature("Item group delete") {
        scenario("Ok") {
            val itemGroup = Await.result(client.storage.saveItemGroup(new client.ItemGroup(None, None, "Already dead", new Date)), Inf)
            Await.result(client.storage.removeItemGroup(itemGroup.id.get), Inf)
            assert(getAll.filter(_.id == itemGroup.id) === Nil)
        }
    }

}
