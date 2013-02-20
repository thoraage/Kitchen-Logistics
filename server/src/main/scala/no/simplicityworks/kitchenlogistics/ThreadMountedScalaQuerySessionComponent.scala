package no.simplicityworks.kitchenlogistics

import util.DynamicVariable
import org.scalaquery.session.Session
import unfiltered.filter.Plan
import javax.servlet.http.HttpServletRequest

trait ThreadMountedScalaQuerySessionComponent {
  this: ProductDatabaseComponent =>

  trait ThreadMountedScalaQuerySession extends ScalaQuerySession {

    private val sessionDynamicVariable = new DynamicVariable[Option[Session]](None)

    override def session = sessionDynamicVariable.value.get

    override def intent = {
      def isDefinedAt(req: HttpServletRequest): Boolean = super.isDefinedAt(req)

      {
        case req =>
          database withSession {
            session =>
              sessionDynamicVariable.withValue(Some(session)) {
                super.intent(req)
              }
          }
      }
    }
  }

}