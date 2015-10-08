package no.simplicityworks.kitchenlogistics

import java.util.Date

import org.scalatest._

import scala.concurrent.Await
import scala.concurrent.duration.Duration.Inf

class ItemGroupSpec extends FeatureSpec with GivenWhenThen with KitLogSpecBase {

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
