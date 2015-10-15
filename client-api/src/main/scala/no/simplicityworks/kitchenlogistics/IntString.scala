package no.simplicityworks.kitchenlogistics

object IntString {
    def unapply(v: String) = try Some(v.toInt)
    catch {
        case _: NumberFormatException => None
    }
}

