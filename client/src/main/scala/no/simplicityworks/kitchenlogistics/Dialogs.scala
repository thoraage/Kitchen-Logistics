package no.simplicityworks.kitchenlogistics

import android.app.Dialog
import android.view.View

object Dialogs {
  var inputSuccessFunctionMap: Map[Int, (String) => Unit] = Map()
}

trait Dialogs {
  this: TypedActivity =>

  var createDialogMap: Map[Int, () => Dialog] = Map()

  def createInputDialog(dialogDiscriminatorId: Int, titleId: Int, messageId: Int, f: (String) => Unit) {
    Dialogs.inputSuccessFunctionMap += (dialogDiscriminatorId -> f)
    createDialog(dialogDiscriminatorId) {
      () =>
        val dialog = new Dialog(this) with TypedDialog
        dialog.setContentView(R.layout.inputdialog)
        dialog.setTitle(titleId)
        dialog.findView(TR.inputDialogMessage).setText(messageId)
        def removeDialog(dialog: Dialog with TypedDialog) {
          Dialogs.inputSuccessFunctionMap -= dialogDiscriminatorId
          createDialogMap -= dialogDiscriminatorId
          dialog.dismiss()
        }
        dialog.findView(TR.inputDialogOk).setOnClickListener {
          (_: View) =>
            val input = dialog.findView(TR.inputDialogField)
            Dialogs.inputSuccessFunctionMap.get(dialogDiscriminatorId).map(_(input.getText.toString))
            input.getText.clear()
            removeDialog(dialog)
        }
        dialog.findView(TR.inputDialogCancel).setOnClickListener {
          (_: View) =>
            dialog.findView(TR.inputDialogField).getText.clear()
            removeDialog(dialog)
        }
        dialog
    }
  }

  def createInfoDialog(dialogDiscriminatorId: Int, titleId: Int, messageId: Int) {
    createDialog(dialogDiscriminatorId) {
      () =>
        val dialog = new Dialog(this) with TypedDialog
        dialog.setContentView(R.layout.infodialog)
        dialog.setTitle(titleId)
        dialog.findView(TR.inputDialogMessage).setText(messageId)
        dialog.findView(TR.inputDialogOk).setOnClickListener {
          (_: View) =>
            createDialogMap -= dialogDiscriminatorId
            dialog.dismiss()
        }
        dialog
    }
  }

  def createDialog(id: Int)(createF: () => Dialog) {
    createDialogMap += (id -> createF)
    showDialog(id)
  }

  override def onCreateDialog(id: Int) = {
    val dialog = createDialogMap.get(id) match {
      case Some(createF) =>
        createF()
      case None =>
        // TODO nothing?
        null
    }
    dialog
  }

}