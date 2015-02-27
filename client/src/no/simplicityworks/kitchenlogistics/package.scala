package no.simplicityworks

import android.view.View
import android.view.View.OnClickListener

import scala.language.implicitConversions

package object kitchenlogistics {

  implicit def toOnClickListener(f: (View) => Unit): OnClickListener = new OnClickListener {
    def onClick(v: View) {
      f(v)
    }
  }

}