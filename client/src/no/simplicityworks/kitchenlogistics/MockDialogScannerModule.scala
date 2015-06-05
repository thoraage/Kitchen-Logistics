package no.simplicityworks.kitchenlogistics

trait MockDialogScannerModule extends ScannerModule with GuiContextModule with DialogsModule {

    override lazy val scanner = new Scanner {

        override def startScanner(f: (String) => Unit) {
            dialogs.createInputDialog(4637286, R.string.mockScanTitle, R.string.mockScanMessage, f)
        }

    }

}