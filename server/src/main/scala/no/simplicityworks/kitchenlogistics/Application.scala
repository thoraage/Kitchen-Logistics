package no.simplicityworks.kitchenlogistics

import org.eclipse.jetty.util.resource.Resource

trait Application extends App {

  val stack: RestPlanModule

  val http = unfiltered.jetty.Http(5000)
  http.current.setBaseResource(Resource.newResource(getClass.getResource("/public").getFile, false))
  stack.plans.foldLeft(http)((http, plan) => http.plan(plan)).run()

}
