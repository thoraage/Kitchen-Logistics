package no.simplicityworks.kitchenlogistics

object ApplicationDevelopment extends Application {

    lazy override val stack = new RestPlanModule with DatabaseModule with H2DatabaseProfileModule

}
