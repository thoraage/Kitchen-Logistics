package no.simplicityworks.kitchenlogistics

trait MockDialogScanner extends Scanner with Dialogs {
  this: TypedActivity =>

  def startScanner(f: (String) => Unit) {
    createInputDialog(4637286, R.string.mockScanTitle, R.string.mockScanMessage, f)
  }

}