package no.simplicityworks.kitchenlogistics

import unfiltered.jetty.Server
import org.eclipse.jetty.server.session.SessionHandler

trait JettyWebPlanComponent extends WebPlanComponent {

  def run() {
    val http: Server = registerWebPlan.foldLeft[Server](unfiltered.jetty.Http(8080))((s, f) => f(s))
    http.current.setSessionHandler(new SessionHandler)
    http.run()
  }

}
