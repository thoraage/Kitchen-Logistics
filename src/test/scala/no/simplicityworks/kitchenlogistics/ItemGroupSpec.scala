package no.simplicityworks.kitchenlogistics

import org.scalatest._
import org.json4s._

import scala.util.Properties

class ItemGroupSpec extends FeatureSpec with GivenWhenThen {

    private val port = 58008
    Properties.setProp("PORT", port.toString)
    val stack = new RestPlanModule with InMemoryDatabaseModule
//    val req = host("localhost", port) / "rest" / "itemGroups" setContentType("application/json", "UTF-8")

    feature("Item group rename") {

        scenario("Sunshine story") {
            Given("Existing item group")
//            req.PUT.setBody("""{name: 'hei'}""").OK(println)
            Thread.sleep(5000)
        }

    }

}
