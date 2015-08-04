package no.simplicityworks.kitchenlogistics

import org.scaloid.common.SActivity

trait GuiContextModule {

    implicit def guiContext: SActivity with TypedFindView
    
}
