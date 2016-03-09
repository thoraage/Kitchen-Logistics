package no.simplicityworks.kitchenlogistics

import java.util.Date

import org.scalatest._

import scala.util.Random

class RestPlanSpec extends FeatureSpec with SpecBase with GivenWhenThen {

    override lazy val app = new RestPlanModule
        with InMemoryDatabaseModule
        with BasicAuthenticationPlanModule

    lazy val product =
        await(client.storage.saveProduct(client.Product(None, "fdjks", "ting", "nob", new Date)))
    def createItemGroup =
        await(client.storage.saveItemGroup(client.ItemGroup(None, None, s"test${Random.nextInt()}", new Date)))
    def createItem(itemGroup: client.ItemGroup) =
        await(client.storage.saveItem(client.Item(None, None, product.id.get, itemGroup.id.get, new Date)))
    def allGroups = await(client.storage.getItemGroups)

    def itemsOf(itemGroup: client.ItemGroup) =
        await(client.storage.findItemsByGroup(Some(itemGroup)))

    def assertForbidden(f: => Any) = assertStatus(403, f)

    def assertStatus(httpStatus: Int, f: => Any): Unit = {
        try {
            val result = f
            fail(s"Received: $result")
        } catch {
            case StatusCodeException(_, status, _) =>
                assert(status === httpStatus)
        }
    }

    feature("Item group get all") {
        scenario("Ok") {
            val itemGroups = app.ItemGroups.getForUser(thoredge)
            assert(allGroups.seq.size === itemGroups.size)
            assert(itemGroups.forall(_.userId === thoredge.id))
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
            val itemGroup = await(client.storage.saveItemGroup(client.ItemGroup(None, None, "Måse", new Date)))
            assert(itemGroup.id !== None)
            assert(allGroups.seq.size === initialCount + 1)
            assert(await(client.storage.getItemGroup(itemGroup.id.get)).name === "Måse")
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
        scenario("Other user forbidden") {
            val itemGroup = createItemGroup
            assertForbidden(await(otherClient.storage.removeItemGroup(itemGroup.id.get)))
        }
    }

    feature("Item get") {
        lazy val itemGroup = createItemGroup
        lazy val otherItemGroup = createItemGroup
        scenario("Ok") {
            createItem(itemGroup)
            createItem(itemGroup)
            val results = itemsOf(itemGroup)
            assert(results.size === 1 && results.head.count === 2)
            assert(itemsOf(otherItemGroup).size === 0)
        }
        scenario("Item is not seen by other user") {
            val item1 = createItem(itemGroup)
            val item2 = createItem(itemGroup)
            val results = await(otherClient.storage.findItemsByGroup(None))
            assert(!results.exists(item => item.lastItemId == item1.id.get || item.lastItemId == item2.id.get))
        }
        scenario("Recent items independent of group") {
            val itemGroupA = await(client.storage.saveItemGroup(new client.ItemGroup(None, None, "A")))
            val item1 = await(client.storage.saveItem(new client.Item(None, None, product.id.get, itemGroupA.id.get)))
            val itemGroupB = await(client.storage.saveItemGroup(new client.ItemGroup(None, None, "B")))
            val product2 = await(client.storage.saveProduct(new client.Product(None, "something", "A thing", "nob")))
            val item2 = await(client.storage.saveItem(new client.Item(None, None, product2.id.get, itemGroupB.id.get)))
            val item3 = await(client.storage.saveItem(new client.Item(None, None, product.id.get, itemGroupB.id.get)))
            println(await(client.storage.findItemsByGroup(None)))
            val results = await(client.storage.getRecentItems(3)).toList
            assert(results.sliding(2).forall(l => l.head.lastItemId > l(1).lastItemId))
            assert(results.size === 3)
        }
    }

    feature("Item update") {
        lazy val itemGroupA = createItemGroup
        lazy val itemGroupB = createItemGroup
        lazy val item = createItem(itemGroupA)
        scenario("Ok") {
            item
            assert(1 === itemsOf(itemGroupA).size)
            assert(0 === itemsOf(itemGroupB).size)
            await(client.storage.saveItem(item.copy(itemGroupId = itemGroupB.id.get)))
            assert(0 === itemsOf(itemGroupA).size)
            assert(1 === itemsOf(itemGroupB).size)
        }
        scenario("Other user forbidden") {
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
        lazy val itemGroup = createItemGroup
        scenario("Ok") {
            val item = createItem(itemGroup)
            client.storage.findItemsByGroup(Some(itemGroup))
            await(client.storage.removeItem(item.id.get))
            assert(0 === itemsOf(itemGroup).size)
        }
        scenario("Other user forbidden") {
            val item = createItem(itemGroup)
            assertForbidden(await(otherClient.storage.removeItem(item.id.get)))
            assert(1 == itemsOf(itemGroup).size)
        }
    }

    feature("Find product") {
        scenario("Ok") {
            val code = s"mycode${Random.nextInt()}"
            assert(0 === await(client.storage.findProductByCode(code)).size)
            await(client.storage.saveProduct(client.Product(None, code, "MyName", "nob", new Date)))
            assert(1 === await(client.storage.findProductByCode(code)).size)
        }
    }

    feature("Search for product") {
        scenario("Ok") {
            val summaries = await(client.storage.searchItems("rang"))
            assert(summaries.seq.size === 0)
            val itemGroup = await(client.storage.saveItemGroup(client.ItemGroup(None, None, "Cupboard")))
            val products = List(await(client.storage.saveProduct(client.Product(None, "gnufdjskfds", "OrAnge", "nob"))),
                await(client.storage.saveProduct(client.Product(None, "jklfds", "Rangpur", "nob"))),
                await(client.storage.saveProduct(client.Product(None, "8493tgtf", "Apple", "nob"))))
            products.foreach(p => await(client.storage.saveItem(client.Item(None, None, p.id.get, itemGroup.id.get))))
            val itemGroup2 = await(otherClient.storage.saveItemGroup(otherClient.ItemGroup(None, None, "Cupboard")))
            await(otherClient.storage.saveItem(otherClient.Item(None, None, products(0).id.get, itemGroup2.id.get)))
            val summaries2 = await(client.storage.searchItems("rang"))
            assert(summaries2.seq.size === 2)
            assert(summaries2.seq.forall(_.count == 1))
        }
    }

    feature("Product rename") {
        lazy val itemGroup = createItemGroup
        def createCode = s"mycode${Random.nextInt()}"
        def createProduct = await(client.storage.saveProduct(client.Product(None, createCode, "MyName", "nob")))
        scenario("Soul owner changes the product") {
            val myProduct = createProduct
            val item = await(client.storage.saveItem(client.Item(None, None, myProduct.id.get, itemGroup.id.get)))
            await(client.storage.saveProduct(myProduct.copy(name = "NewName")))
            assert(await(client.storage.findProductByCode(myProduct.code)).map(_.name) === List("NewName"))
        }
        scenario("Multiple owners leads to new product and deletes it if the name changes back") {
            val myProduct = createProduct
            val item = await(client.storage.saveItem(client.Item(None, None, myProduct.id.get, itemGroup.id.get)))
            val otherUserItemGroup = await(otherClient.storage.saveItemGroup(otherClient.ItemGroup(None, None, "Yeah")))
            val otherUserItem = await(otherClient.storage.saveItem(otherClient.Item(None, None, myProduct.id.get, otherUserItemGroup.id.get)))

            await(client.storage.saveProduct(myProduct.copy(name = "NewName")))

            assert(await(client.storage.getProduct(myProduct.id.get)).name === "MyName")
            assert(await(otherClient.storage.getItem(otherUserItem.id.get)).productId === myProduct.id.get)
            val newProductId = await(client.storage.getItem(item.id.get)).productId
            assert(newProductId !== myProduct.id.get)
            val newProduct = await(client.storage.getProduct(newProductId))
            assert(newProduct.name === "NewName")

            await(client.storage.saveProduct(newProduct.copy(name = "MyName")))

            val newerProductId = await(client.storage.getItem(item.id.get)).productId
            assert(newerProductId === myProduct.id.get)
            assertStatus(404, await(client.storage.getProduct(newProductId)))
        }
    }
}
