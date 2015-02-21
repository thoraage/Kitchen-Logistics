package no.simplicityworks.kitchenlogistics

object ApplicationDevelopment extends Application {

    lazy override val stack = new RestPlanModule with DatabaseModule {

        override lazy val databaseProfile = new DatabaseProfile {
            override val password = ""
            override val username = "root"
            override val driverClass = "org.h2.Driver"
            override val jdbcUrl = "jdbc:h2:mem:test"
            override val driver = scala.slick.driver.H2Driver
            override val generation = DatabaseGeneration.slickDdl
        }

    }

}
