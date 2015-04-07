package no.simplicityworks

import android.view.View
import android.view.View.OnClickListener

import scala.language.implicitConversions

package object kitchenlogistics {

  implicit class ExtendedTypedViewHolder(val typedViewHolder: TypedViewHolder) {
    def findResource[V <: View](tr: TypedResource[V]): V = typedViewHolder.findViewById(tr.id).asInstanceOf[V]
  }

}