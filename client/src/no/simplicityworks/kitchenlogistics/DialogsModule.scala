package no.simplicityworks.kitchenlogistics

import android.app.AlertDialog.Builder
import android.app.{AlertDialog, Dialog}
import android.content.DialogInterface
import android.content.DialogInterface.OnClickListener
import android.view.LayoutInflater
import no.simplicityworks.kitchenlogistics.TypedResource.TypedView
import org.scaloid.common._

trait DialogsModule extends GuiContextModule {

    var inputSuccessFunctionMap: Map[Int, (String) => Unit] = Map()
    var createDialogMap: Map[Int, () => Dialog] = Map()

    def dialogs = new Dialogs

    class Dialogs {

        def withMessage(titleId: Int, messageId: Int) = {
            guiContext.runOnUiThread {
                new AlertDialog.Builder(guiContext)
                    .setTitle(titleId)
                    .setMessage(messageId)
                    .setPositiveButton(R.string.inputDialogClose, null)
                    .show()
                ()
            }
        }

        def withField(titleId: Int, validate: (String, String => Unit) => Unit) {
            def show(text: String = "", feedback: String = "") {
                val builder = new Builder(guiContext)
                builder.setTitle(titleId)
                builder.setNegativeButton(R.string.inputDialogCancel, new OnClickListener {
                    override def onClick(dialog: DialogInterface, which: Int): Unit = dialog.cancel()
                })
                val inflater = LayoutInflater.from(guiContext)
                val inputView = inflater.inflate(R.layout.input_field, null, false)
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

    }

}