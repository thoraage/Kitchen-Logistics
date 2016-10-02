package no.simplicityworks.kitchenlogistics

import org.scalatest.{FeatureSpec, GivenWhenThen}

class DistributionSpec extends FeatureSpec with GivenWhenThen {

    feature("Build ") {
        val spans = List[Long](100, 10, 1)
        scenario("1") {
            assert(Distribution.distribute(spans, 1000) === List(10, 0, 0))
        }
        scenario("2") {
            assert(Distribution.distribute(spans, 984) === List(9, 8, 4))
        }
        scenario("3") {
            assert(Distribution.distribute(spans, 84) === List(0, 8, 4))
        }
        scenario("4") {
            assert(Distribution.distribute(spans, 184) === List(1, 8, 4))
        }
    }

}
