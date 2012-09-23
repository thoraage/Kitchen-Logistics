package no.simplicityworks.kitchenlogistics

import unfiltered.jetty.Server

trait JettyWebPlanComponent extends WebPlanComponent {

  def run() {
    registerWebPlan.foldLeft[Server](unfiltered.jetty.Http(8080))((s, f) => f(s)).run()
  }

}
