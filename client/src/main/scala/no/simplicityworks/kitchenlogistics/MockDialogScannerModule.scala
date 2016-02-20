package no.simplicityworks.kitchenlogistics

import org.scaloid.common._

import scala.concurrent.Promise
import scala.util.Success

trait MockDialogScannerModule extends ScannerModule with GuiContextModule with DialogsModule {

    override lazy val scanner = new Scanner {

        override def startScanner() = {
            val promise = Promise[Option[String]]()
            dialogs.withField(R.string.mockScanTitle.r2String, "", (code, _) => promise.complete(Success(Some(code))))
            promise.future
        }

    }

}