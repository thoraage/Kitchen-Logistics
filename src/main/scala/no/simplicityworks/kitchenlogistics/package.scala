package no.simplicityworks

import android.view.View
import android.view.View.OnClickListener

package object kitchenlogistics {

  implicit def toOnClickListener(f: (View) => Unit) = new OnClickListener {
    def onClick(v: View) {
      f(v)
    }
  }

}