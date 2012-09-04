package no.simplicityworks.kitchenlogistics

import util.DynamicVariable
import org.scalaquery.session.Session

trait ThreadMountedScalaQuerySession extends ScalaQuerySession {

  private val sessionDynamicVariable = new DynamicVariable[Option[Session]](None)

  override def session = sessionDynamicVariable.value.get

  override def intent = {
    case req =>
      ProductDb.database withSession {
        session =>
          sessionDynamicVariable.withValue(Some(session)) {
            super.intent(req)
          }
      }
  }

}