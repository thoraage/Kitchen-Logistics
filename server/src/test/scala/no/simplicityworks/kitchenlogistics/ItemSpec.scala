package no.simplicityworks.kitchenlogistics

import java.util.Date

import org.scalatest.{FeatureSpec, GivenWhenThen}

import scala.concurrent.duration.Duration.Inf
import scala.concurrent.{Await, Future}
import scala.util.Random

class ItemSpec extends FeatureSpec with GivenWhenThen with KitLogSpecBase {

    implicit def await[T](future: Future[T]): T = Await.result(future, Inf)

    val product: client.Product = client.storage.saveProduct(new client.Product(None, "fdjks", "ting", new Date))
    def createItemGroup: client.ItemGroup = client.storage.saveItemGroup(new client.ItemGroup(None, None, s"test${Random.nextInt()}", new Date))
    def createItem(itemGroup: client.ItemGroup): client.Item =
        client.storage.saveItem(new client.Item(None, None, product.id.get, itemGroup.id.get, new Date))

    feature("Item get") {
        scenario("Ok") {
            val itemGroup = createItemGroup
            val otherItemGroup = createItemGroup
            createItem(itemGroup)
            createItem(itemGroup)
            val result = client.storage.findItemsByGroup(Some(itemGroup))
            assert(result.size === 1 && result.head.count === 2)
            assert(client.storage.findItemsByGroup(Some(otherItemGroup)).size === 0)
        }
    }

    feature("Item create") {
        scenario("Ok") {
            val itemGroup = createItemGroup
            createItem(itemGroup)
            assert(1 === client.storage.findItemsByGroup(Some(itemGroup)).size)
        }
    }

    feature("Item delete") {
        scenario("Ok") {
            val itemGroup = createItemGroup
            val item = createItem(itemGroup)
            client.storage.findItemsByGroup(Some(itemGroup))
            await(client.storage.removeItem(item.id.get))
            assert(0 === client.storage.findItemsByGroup(Some(itemGroup)).size)
        }
    }

}
