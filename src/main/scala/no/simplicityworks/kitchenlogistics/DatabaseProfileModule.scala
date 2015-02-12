package no.simplicityworks.kitchenlogistics

trait DatabaseProfileModule {
    val databaseProfile: DatabaseProfile
}

trait DatabaseProfile {
    val password: String
    val username: String
    val driverClass: String
    val jdbcUrl: String
    val driver: scala.slick.driver.ExtendedDriver
    val generation: DatabaseGeneration.Type
}

object DatabaseGeneration extends Enumeration {
    type Type = Value
    val slickDdl, flyway = Value
}