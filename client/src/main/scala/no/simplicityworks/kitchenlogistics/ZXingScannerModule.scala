package no.simplicityworks.kitchenlogistics

import android.app.Activity
import android.content.{ActivityNotFoundException, Intent}

object ZXingScannerModule {
    val scannerRequestCode = 6739477
    var onScanSuccess: Option[(String) => Unit] = None
}

trait ZXingScannerModule extends ScannerModule with DialogsModule {
    this: Activity with TypedFindView with StorageModule =>

    override lazy val scanner = new Scanner {

        override def startScanner(f: (String) => Unit) {
            try {
                ZXingScannerModule.onScanSuccess = Some(f)
                val intent = new Intent("com.google.zxing.client.android.SCAN")
                startActivityForResult(intent, ZXingScannerModule.scannerRequestCode);
            } catch {
                case e: ActivityNotFoundException =>
                    ZXingScannerModule.onScanSuccess = None
                    dialogs.withMessage(R.string.scanNotInstalledTitle, R.string.scanNotInstalledMessage)
            }
        }

        def onActivityResult(requestCode: Int, resultCode: Int, intent: Intent) {
            if (requestCode == ZXingScannerModule.scannerRequestCode) {
                if (resultCode == Activity.RESULT_OK) {
                    val code = intent.getStringExtra("SCAN_RESULT")
                    ZXingScannerModule.onScanSuccess.foreach(_(code))
                } else if (resultCode == Activity.RESULT_CANCELED) {
                    dialogs.withMessage(R.string.noResultTitle, R.string.noResultMessage)
                }
                ZXingScannerModule.onScanSuccess = None
            }
            // TODO super.onActivityResult(requestCode, resultCode, intent)
        }

    }

}