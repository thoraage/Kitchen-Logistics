package no.simplicityworks.kitchenlogistics

import scala.concurrent.Future

trait ScannerModule {

  def scanner: Scanner

}

trait Scanner {

  def startScanner(): Future[Option[String]]

}
