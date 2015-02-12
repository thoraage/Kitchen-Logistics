package no.simplicityworks.kitchenlogistics

trait ProductionDatabaseProfileModule extends DatabaseProfileModule {

    override lazy val databaseProfile = new DatabaseProfile {
        override val driver = scala.slick.driver.PostgresDriver
        override val generation = DatabaseGeneration.flyway
        override val password: String = "kitlog"
        override val username: String = "kitlog"
        override val jdbcUrl: String = "jdbc:postgresql://localhost/kitlog"
        override val driverClass: String = "org.postgresql.Driver"
    }

}
