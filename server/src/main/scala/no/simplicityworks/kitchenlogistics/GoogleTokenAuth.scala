package no.simplicityworks.kitchenlogistics

import com.migcomponents.migbase64.Base64
import unfiltered.request.{Authorization, HttpRequest}

object GoogleTokenAuth {

    val AuthRegex = "GoogleToken +(.*) *".r

    object Base64String {
        def unapply(s: String) = Option(new String(Base64.decode(s)))
    }

    def unapply[T](r: HttpRequest[T]) = r match {
        case Authorization(AuthRegex(Base64String(token))) => Some(token)
        case _ => None
    }

}
