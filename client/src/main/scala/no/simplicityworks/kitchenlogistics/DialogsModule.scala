package no.simplicityworks.kitchenlogistics

import android.app.AlertDialog.Builder
import android.app.{AlertDialog, Dialog}
import android.content.DialogInterface
import android.content.DialogInterface.OnClickListener
import android.text.{Editable, TextWatcher}
import android.view.LayoutInflater
import no.simplicityworks.kitchenlogistics.TypedResource.TypedView
import org.scaloid.common._

trait DialogsModule extends GuiContextModule with StableValuesModule {

    var inputSuccessFunctionMap: Map[Int, (String) => Unit] = Map()
    var createDialogMap: Map[Int, () => Dialog] = Map()

    def dialogs = new Dialogs

    class Dialogs {
        def confirm(titleId: Int, andThen: => Unit) {
            stick(start = true, () =>
                new AlertDialog.Builder(guiContext)
                    .setTitle(titleId)
                    .setNegativeButton(R.string.no, null)
                    .setPositiveButton(R.string.yes, new OnClickListener {
                        override def onClick(dialog: DialogInterface, which: Int) = andThen
                    })
                    .setOnDismissListener(unstick)
                    .show())
        }


        def withMessage(titleId: Int, messageId: Int) = {
            stick(start = true, () =>
                new AlertDialog.Builder(guiContext)
                    .setTitle(titleId)
                    .setMessage(messageId)
                    .setPositiveButton(R.string.inputDialogClose, null)
                    .setOnDismissListener(unstick)
                    .show())
        }

        def withField(title: String, text: String, validate: (String, String => Unit) => Unit) {
            def show(text: String, feedback: String = "") {
                val builder = new Builder(guiContext)
                builder.setTitle(title)
                builder.setNegativeButton(R.string.inputDialogCancel, new OnClickListener {
                    override def onClick(dialog: DialogInterface, which: Int): Unit = dialog.cancel()
                })
                val inflater = LayoutInflater.from(guiContext)
                val inputView = inflater.inflate(R.layout.input_field, null, false)
                builder.setView(inputView)
                val dialogInputField = inputView.findView(TR.dialogInputField)
                val dialogInputFeedback = inputView.findView(TR.dialogInputFeedback)
                dialogInputField.setText(text)
                dialogInputField.setSelection(text.length)
                dialogInputFeedback.setText(feedback)
                dialogInputField.addTextChangedListener(new TextWatcher {
                    override def beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) = ()
                    override def onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) = ()
                    override def afterTextChanged(s: Editable) {
                        stick(start = false, () => show(dialogInputField.getText.toString, feedback))
                    }
                })
                builder.setPositiveButton(R.string.inputDialogOk, new OnClickListener {
                    override def onClick(d: DialogInterface, which: Int) {
                        val name = dialogInputField.getText.toString
                        validate(name, feedback => stick(start = false, () => show(name, feedback)))
                    }
                })
                builder.setOnDismissListener(unstick)
                builder.show()
            }
            stick(start = true, () => show(text))
        }

        val unstick = new DialogInterface.OnDismissListener {
            override def onDismiss(dialog: DialogInterface) {
                stableValues.dialogStickFunction = None
            }
        }

        def stick(start: Boolean, f: () => Unit) = {
            stableValues.dialogStickFunction = Some(f)
            if (start) restick()
        }

        def restick() {
            stableValues.dialogStickFunction.foreach(f => runOnUiThread(f()))
        }

    }

}
