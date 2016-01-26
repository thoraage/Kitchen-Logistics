package no.simplicityworks.kitchenlogistics

import org.scaloid.common.SActivity

import scala.concurrent.Future

trait GuiContextModule {

    implicit def guiContext: GuiContext
    
}

trait GuiContext extends SActivity with TypedFindView {
    def futureOnUiThread[T](f: => T): Future[T]
}