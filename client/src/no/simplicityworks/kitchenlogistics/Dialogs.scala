package no.simplicityworks.kitchenlogistics

import android.app.Dialog
import android.view.View

trait Dialogs {
  this: TypedActivity =>

  def createInputDialog(dialogDiscriminatorId: Int, titleId: Int, messageId: Int, f: (String) => Unit) {
    inputSuccessFunctionMap += (dialogDiscriminatorId -> f)
    createDialog(dialogDiscriminatorId) {
      () =>
        val dialog = new Dialog(this) with TypedDialog
        dialog.setContentView(R.layout.inputdialog)
        dialog.setTitle(titleId)
        dialog.findView(TR.inputDialogMessage).setText(messageId)
        def removeDialog(dialog: Dialog with TypedDialog) {
          inputSuccessFunctionMap -= dialogDiscriminatorId
          dialog.dismiss()
        }
        dialog.findView(TR.inputDialogOk).setOnClickListener {
          (_: View) =>
            val input = dialog.findView(TR.inputDialogField)
            inputSuccessFunctionMap.get(dialogDiscriminatorId).map(_(input.getText.toString))
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
            dialog.dismiss()
        }
        dialog
    }
  }

  var inputSuccessFunctionMap: Map[Int, (String) => Unit] = Map()
  var createDialogMap: Map[Int, () => Dialog] = Map()

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
    createDialogMap -= id
    dialog
  }

}