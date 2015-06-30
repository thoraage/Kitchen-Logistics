package no.simplicityworks.kitchenlogistics

import android.app.Activity

trait GuiContextModule {

    implicit def guiContext: Activity with TypedFindView
    
}
