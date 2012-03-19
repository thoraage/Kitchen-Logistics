package no.simplicityworks.kitchenlogistics

import android.app.Dialog
import android.view.View

trait Dialogs {
  this: TypedActivity =>

  def createInputDialog(dialogDiscriminatorId: Int, titleId: Int, messageId: Int, f: (String) => Unit) {
    createDialog(dialogDiscriminatorId) {
      () =>
        val dialog = new Dialog(this) with TypedDialog
        dialog.setContentView(R.layout.inputdialog)
        dialog.setTitle(titleId)
        dialog.findView(TR.inputDialogMessage).setText(messageId)
        dialog.findView(TR.inputDialogOk).setOnClickListener {
          (_: View) =>
            f(dialog.findView(TR.inputDialogField).getText.toString)
            dialog.dismiss()
        }
        dialog.findView(TR.inputDialogCancel).setOnClickListener {
          (_: View) =>
            dialog.dismiss()
        }
        dialog
    }
  }

  var dialogMap: Map[Int, () => Dialog] = Map()

  def createDialog(id: Int)(f: () => Dialog) {
    dialogMap += (id -> f)
    showDialog(id)
  }

  override def onCreateDialog(id: Int) = dialogMap.get(id) match {
    case Some(f) =>
      dialogMap -= id
      f()
    case None =>
      // TODO nothing?
      null
  }

}