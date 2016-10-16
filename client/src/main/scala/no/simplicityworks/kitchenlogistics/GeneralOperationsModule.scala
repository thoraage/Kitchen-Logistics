package no.simplicityworks.kitchenlogistics

import android.util.Log
import org.scaloid.common.WidgetHelpers

trait GeneralOperationsModule extends GuiContextModule {

    def generalOperations = new GeneralOperations

    class GeneralOperations {

        def notifyUpdated(message: CharSequence) = WidgetHelpers.toast(message)

        def handleFailure(throwable: Throwable) {
            Log.e(getClass.getSimpleName, "GUI error", throwable)
            notifyUpdated(R.string.errorIntro + throwable.getMessage)
        }

    }

}
