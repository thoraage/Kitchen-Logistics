package no.simplicityworks.kitchenlogistics

import android.content.DialogInterface.OnClickListener
import android.content.{Context, DialogInterface}
import android.support.v7.app.AlertDialog.Builder
import android.view.LayoutInflater
import no.simplicityworks.kitchenlogistics.TypedResource.TypedView
import org.scaloid.common._

class DialogWithField(titleId: Int, validate: (String, String => Unit) => Unit)(implicit guiContext: Context) {

    private def show(text: String = "", feedback: String = "") {
        val builder = new Builder(guiContext)
        builder.setTitle(titleId)
        builder.setNegativeButton(R.string.inputDialogCancel, new OnClickListener {
            override def onClick(dialog: DialogInterface, which: Int): Unit = dialog.cancel()
        })
        val inflater = LayoutInflater.from(guiContext)
        val inputView = inflater.inflate(R.layout.inputfield, null, false)
        builder.setView(inputView)
        val dialogInputField = inputView.findView(TR.dialogInputField)
        val dialogInputFeedback = inputView.findView(TR.dialogInputFeedback)
        dialogInputField.setText(text)
        dialogInputFeedback.setText(feedback)
        builder.setPositiveButton(R.string.inputDialogOk, new OnClickListener {
            override def onClick(d: DialogInterface, which: Int) {
                val name = dialogInputField.getText.toString
                validate(name, runOnUiThread(show(name, _)))
            }
        })
        builder.show()
    }
    show()

}
