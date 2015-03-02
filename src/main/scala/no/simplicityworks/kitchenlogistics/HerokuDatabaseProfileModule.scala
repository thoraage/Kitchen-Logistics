package no.simplicityworks.kitchenlogistics

import java.net.URI

trait HerokuDatabaseProfileModule extends DatabaseProfileModule {

    override lazy val databaseProfile = new DatabaseProfile {
        private val dbUri = new URI(System.getenv("DATABASE_URL"))
        override val generation = DatabaseGeneration.flyway
        override val password: String = dbUri.getUserInfo.split(":")(1)
        override val username: String = dbUri.getUserInfo.split(":")(0)
        override val jdbcUrl: String = "jdbc:postgresql://" + dbUri.getHost + dbUri.getPath
        override val driverClass: String = "org.postgresql.Driver"
    }

}
