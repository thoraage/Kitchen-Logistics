package no.simplicityworks.kitchenlogistics

import org.scalaquery.session.Session
import unfiltered.filter.Plan

trait ScalaQuerySession extends Plan {

  implicit def session: Session

  def intent = {
    case _ => sys.error("Need to be backed by a real plan intent")
  }

}