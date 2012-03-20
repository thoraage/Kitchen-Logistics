package no.simplicityworks.kitchenlogistics

import android.app.Activity
import android.content.{ActivityNotFoundException, Intent}

object ZXingScanner {
  val scannerRequestCode = 6739477
}

trait ZXingScanner extends Scanner with Dialogs {
  this: Activity with TypedActivity with Storage =>

  var onScanSuccess: Option[(String) => Unit] = None

  override def startScanner(f: (String) => Unit) {
    try {
      this.onScanSuccess = Some(f)
      val intent = new Intent("com.google.zxing.client.android.SCAN");
      startActivityForResult(intent, ZXingScanner.scannerRequestCode);
    } catch {
      case e: ActivityNotFoundException =>
      this.onScanSuccess = None
      createInfoDialog(935723, R.string.scanNotInstalledTitle, R.string.scanNotInstalledMessage)
    }
  }

  override def onActivityResult(requestCode: Int, resultCode: Int, intent: Intent) {
    if (requestCode == ZXingScanner.scannerRequestCode) {
      if (resultCode == Activity.RESULT_OK) {
        val code = intent.getStringExtra("SCAN_RESULT")
        onScanSuccess.map(_(code))
      } else if (resultCode == Activity.RESULT_CANCELED) {
        createInfoDialog(8768343, R.string.noResultTitle, R.string.noResultMessage)
      }
      onScanSuccess = None
    }
    // TODO super.onActivityResult(requestCode, resultCode, intent)
  }

}