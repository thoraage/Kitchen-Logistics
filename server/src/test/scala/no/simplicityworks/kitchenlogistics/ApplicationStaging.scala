package no.simplicityworks.kitchenlogistics

object ApplicationStaging extends Application {

    lazy override val stack = new RestPlanModule
        with BasicAuthenticationPlanModule
        with DatabaseModule {

        override lazy val databaseProfile = new DatabaseProfile {
            override val generation = DatabaseGeneration.flyway
            override val password: String = "kitlog"
            override val username: String = "kitlog"
            override val jdbcUrl: String = "jdbc:postgresql://localhost/kitlog"
            override val driverClass: String = "org.postgresql.Driver"
        }

    }

}
