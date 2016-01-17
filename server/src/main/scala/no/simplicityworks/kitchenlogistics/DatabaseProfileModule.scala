package no.simplicityworks.kitchenlogistics

trait DatabaseProfileModule {
    val databaseProfile: DatabaseProfile
}

trait DatabaseProfile {
    val password: String
    val username: String
    val driverClass: String
    val jdbcUrl: String
    val generation: DatabaseGeneration.Type
    lazy val explainPlan: Boolean = false
}

object DatabaseGeneration extends Enumeration {
    type Type = Value
    val slickDdl, flyway = Value
}