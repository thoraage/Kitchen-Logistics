package no.simplicityworks.kitchenlogistics

import unfiltered.jetty.Server

trait StaticContentWebComponent extends WebPlanComponent {

  override def registerWebPlan = ((server: Server) => server.context("/") {
    _.resources(getClass.getResource("/public"))
  }) :: super.registerWebPlan

}
