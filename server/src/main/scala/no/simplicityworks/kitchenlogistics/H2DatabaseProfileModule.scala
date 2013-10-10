package no.simplicityworks.kitchenlogistics

trait H2DatabaseProfileModule extends DatabaseProfileModule {

  lazy val driver = scala.slick.driver.H2Driver

}
