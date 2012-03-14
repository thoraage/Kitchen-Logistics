package no.simplicityworks.kitchenlogistics

import android.app.Activity
import android.content.{ActivityNotFoundException, Intent}

object ZXingScanner {
  val scannerRequestCode = 6739477
}

trait ZXingScanner extends Scanner {
  this: Activity with TypedActivity with Storage =>

  var f: Option[(String) => Unit] = None

  override def startScanner(f: (String) => Unit) {
    try {
      this.f = Some(f)
      val intent = new Intent("com.google.zxing.client.android.SCAN");
      startActivityForResult(intent, ZXingScanner.scannerRequestCode);
    } catch {
      case e: ActivityNotFoundException =>
        // TODO
    }
  }

  override def onActivityResult(requestCode: Int, resultCode: Int, intent: Intent) {
    if (requestCode == ZXingScanner.scannerRequestCode) {
      if (resultCode == Activity.RESULT_OK) {
        val code = intent.getStringExtra("SCAN_RESULT")
        f.map(_(code))
      } else if (resultCode == Activity.RESULT_CANCELED) {
        // TODO
      }
    }
    // TODO super.onActivityResult(requestCode, resultCode, intent)
  }

}