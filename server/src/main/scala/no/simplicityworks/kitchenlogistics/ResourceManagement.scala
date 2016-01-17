package no.simplicityworks.kitchenlogistics

import scala.language.reflectiveCalls

/**
  * @see http://polyglot-window.blogspot.no/2009/03/arm-blocks-in-scala-revisited.html
  * @see http://stackoverflow.com/questions/2207425/what-automatic-resource-management-alternatives-exists-for-scala
  */
object ResourceManagement {

    def using[T <: { def close() }, R]
    (resource: T)
    (block: T => R): R =  {
        try {
            block(resource)
        } finally {
            if (resource != null) resource.close()
        }
    }

}
