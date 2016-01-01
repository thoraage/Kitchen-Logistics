package no.simplicityworks.kitchenlogistics

import android.app.Activity
import android.content.{ActivityNotFoundException, Intent}

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.control.NonFatal

trait ZXingScannerModule extends ScannerModule with DialogsModule with GuiContextModule with ActivityIntentBrokerModule {

    override lazy val scanner = new Scanner {

        override def startScanner(): Future[Option[String]] = {
            try {
                val requestIntent = new Intent("com.google.zxing.client.android.SCAN")
                activityIntentBroker.getResponseOn(guiContext.startActivityForResult(requestIntent, _)).map {
                    case (Activity.RESULT_OK, responseIntent) =>
                        val code = responseIntent.getStringExtra("SCAN_RESULT")
                        if (code == null) sys.error("Didn't receive scan result")
                        Some(code)
                    case (Activity.RESULT_CANCELED, _) =>
                        dialogs.withMessage(R.string.noResultTitle, R.string.noResultMessage)
                        None
                }
            } catch {
                case e: ActivityNotFoundException =>
                    dialogs.withMessage(R.string.scanNotInstalledTitle, R.string.scanNotInstalledMessage)
                    Future.successful(None)
                case NonFatal(e) =>
                    Future.failed(e)
            }
        }

    }

}