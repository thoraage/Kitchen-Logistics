package no.simplicityworks.kitchenlogistics

trait MockDialogScannerModule extends ScannerModule with GuiContextModule with DialogsModule {

    override lazy val scanner = new Scanner {

        override def startScanner(f: (String) => Unit) {
            new DialogWithField(R.string.mockScanTitle, (code, _) => f(code))
        }

    }

}