package no.simplicityworks.kitchenlogistics

object ApplicationProduction extends Application {

    lazy override val stack = new RestPlanModule
        with DatabaseModule
        with HerokuDatabaseProfileModule
        with BasicAuthenticationPlanModule

}
