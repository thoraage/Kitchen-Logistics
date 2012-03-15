package no.simplicityworks.kitchenlogistics

import android.view.View
import android.app.{Activity, Dialog}

object MockDialogScanner {
  val scanDialogId = 4637286
}

trait MockDialogScanner extends Scanner {
  this: TypedActivity =>

  private var f: Option[(String) => Unit] = None

  def startScanner(f: (String) => Unit) {
    this.f = Some(f)
    showDialog(MockDialogScanner.scanDialogId)
  }

  override def onCreateDialog(id: Int) = id match {
    case MockDialogScanner.scanDialogId =>
      val dialog = new Dialog(this) with TypedDialog
      dialog.setContentView(R.layout.inputdialog)
      dialog.setTitle(R.string.mockScanTitle)
      dialog.findView(TR.inputDialogMessage).setText(R.string.mockScanMessage)
      dialog.findView(TR.inputDialogOk).setOnClickListener {
        (_: View) =>
          f.map(_(dialog.findView(TR.inputDialogField).getText.toString))
          dialog.dismiss()
      }
      dialog.findView(TR.inputDialogCancel).setOnClickListener {
        (_: View) =>
          dialog.dismiss()
      }
      dialog
  }

}