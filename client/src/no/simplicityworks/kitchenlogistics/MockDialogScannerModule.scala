package no.simplicityworks.kitchenlogistics

trait MockDialogScannerModule extends ScannerModule with Dialogs {
  this: TypedActivity =>

  override lazy val scanner = new Scanner {

    override def startScanner(f: (String) => Unit) {
      createInputDialog(4637286, R.string.mockScanTitle, R.string.mockScanMessage, f)
    }

  }

}