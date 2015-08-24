package no.simplicityworks.kitchenlogistics

import android.app.Dialog
import no.simplicityworks.kitchenlogistics.TypedResource.TypedDialog
import org.scaloid.common._

trait DialogsModule extends GuiContextModule {

    var inputSuccessFunctionMap: Map[Int, (String) => Unit] = Map()
    var createDialogMap: Map[Int, () => Dialog] = Map()

    def dialogs = new Dialogs

    class Dialogs {
        def onCreateDialog(id: Int): Dialog = {
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

        def createInfoDialog(dialogDiscriminatorId: Int, titleId: Int, messageId: Int) {
            createDialog(dialogDiscriminatorId) {
                () =>
                    val dialog = new Dialog(guiContext) with TypedFindView
                    dialog.setContentView(R.layout.infodialog)
                    dialog.setTitle(titleId)
                    dialog.findView(TR.inputDialogMessage).setText(messageId)
                    dialog.findView(TR.inputDialogOk).onClick(dialog.dismiss())
                    dialog
            }
        }

        def createDialog(id: Int)(createF: () => Dialog) {
            runOnUiThread {
                createDialogMap += (id -> createF)
                guiContext.showDialog(id)
            }
        }

    }

}