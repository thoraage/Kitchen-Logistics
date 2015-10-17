package no.simplicityworks.kitchenlogistics

import java.util.Date

import org.scalatest._

import scala.concurrent.{Future, Await}
import scala.concurrent.duration.Duration.Inf
import scala.util.Random

class RestSpec extends FeatureSpec with GivenWhenThen with KitLogSpecBase {
    def await[T](future: Future[T]): T = Await.result(future, Inf)


    val user = stack.Users("thoredge").get
    val product =
        await(client.storage.saveProduct(client.Product(None, "fdjks", "ting", new Date)))
    def createItemGroup =
        await(client.storage.saveItemGroup(client.ItemGroup(None, None, s"test${Random.nextInt()}", new Date)))
    def createItem(itemGroup: client.ItemGroup) =
        await(client.storage.saveItem(client.Item(None, None, product.id.get, itemGroup.id.get, new Date)))
    def allGroups = await(client.storage.getItemGroups)

    def itemsOf(itemGroup: client.ItemGroup) =
        await(client.storage.findItemsByGroup(Some(itemGroup)))

    def assertForbidden(f: => Any) {
        try {
            val result = f
            fail(s"Received: $result")
        } catch {
            case StatusCodeException(_, status, _) =>
                assert(status === 403)
        }
    }
    feature("Item group get all") {
        scenario("Ok") {
            val itemGroups = stack.ItemGroups.getForUser(user)
            assert(allGroups.seq.size === itemGroups.size)
            assert(itemGroups.forall(_.userId === user.id))
        }
        scenario("Item group is not seen by other user") {
            val itemGroup = createItemGroup
            val itemGroups = await(otherClient.storage.getItemGroups)
            assert(!itemGroups.exists(_.id == itemGroup.id))
        }
    }

    feature("Item group create") {
        scenario("Ok") {
            val initialCount = allGroups.size
            val itemGroup = await(client.storage.saveItemGroup(client.ItemGroup(None, None, "Mine", new Date)))
            assert(itemGroup.id !== None)
            assert(allGroups.seq.size === initialCount + 1)
        }
    }

    feature("Item group rename") {
        scenario("Ok") {
            val itemGroup = allGroups.head
            await(client.storage.saveItemGroup(itemGroup.copy(name = "New name")))
            assert(allGroups.filter(_.id == itemGroup.id).head.name === "New name")
        }
        scenario("Other user forbidden") {
            val itemGroup = createItemGroup
            val newName = "New name"
            assertForbidden {
                await(otherClient.storage.saveItemGroup(otherClient.ItemGroup(itemGroup.id, itemGroup.userId, newName, new Date())))
            }
            assert(await(client.storage.getItemGroups).filter(_.id == itemGroup.id).forall(_.name != newName))
        }
    }

    feature("Item group delete") {
        scenario("Ok") {
            val itemGroup = await(client.storage.saveItemGroup(client.ItemGroup(None, None, "Already dead", new Date)))
            await(client.storage.removeItemGroup(itemGroup.id.get))
            assert(allGroups.filter(_.id == itemGroup.id) === Nil)
        }
        scenario("Forbidden") {
            val itemGroup = createItemGroup
            assertForbidden(await(otherClient.storage.removeItemGroup(itemGroup.id.get)))
        }
    }

    feature("Item get") {
        scenario("Ok") {
            val itemGroup = createItemGroup
            val otherItemGroup = createItemGroup
            createItem(itemGroup)
            createItem(itemGroup)
            val result = itemsOf(itemGroup)
            assert(result.size === 1 && result.head.count === 2)
            assert(itemsOf(otherItemGroup).size === 0)
        }
    }

    // TODO scenario unauthorized

    feature("Item update") {
        val itemGroupA = createItemGroup
        val itemGroupB = createItemGroup
        val item = createItem(itemGroupA)
        assert(1 === itemsOf(itemGroupA).size)
        assert(0 === itemsOf(itemGroupB).size)
        scenario("Ok") {
            await(client.storage.saveItem(item.copy(itemGroupId = itemGroupB.id.get)))
            assert(0 === itemsOf(itemGroupA).size)
            assert(1 === itemsOf(itemGroupB).size)
        }
        scenario("Forbidden") {
            val itemGroupC = createItemGroup
            val newItem = otherClient.Item(item.id, item.userId, item.productId, itemGroupC.id.get, item.created)
            assertForbidden(await(otherClient.storage.saveItem(newItem)))
            assert(0 === itemsOf(itemGroupC).size)
        }
    }

    feature("Item create") {
        scenario("Ok") {
            val itemGroup = createItemGroup
            createItem(itemGroup)
            assert(1 === itemsOf(itemGroup).size)
        }
    }

    feature("Item delete") {
        scenario("Ok") {
            val itemGroup = createItemGroup
            val item = createItem(itemGroup)
            client.storage.findItemsByGroup(Some(itemGroup))
            await(client.storage.removeItem(item.id.get))
            assert(0 === itemsOf(itemGroup).size)
        }
    }

    feature("Find product") {
        scenario("Ok") {
            val code = s"mycode${Random.nextInt()}"
            assert(0 === await(client.storage.findProductByCode(code)).size)
            await(client.storage.saveProduct(client.Product(None, code, "MyName", new Date)))
            assert(1 === await(client.storage.findProductByCode(code)).size)
        }
    }

}
