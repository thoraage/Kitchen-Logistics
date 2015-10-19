package no.simplicityworks.kitchenlogistics

trait ScannerModule {

  def scanner: Scanner

}

trait Scanner {

  def startScanner(f: (String) => Unit)

}
