package no.simplicityworks.kitchenlogistics

trait PostgresqlDatabaseProfileModule extends DatabaseProfileModule {

  lazy val driver = scala.slick.driver.PostgresDriver

}
