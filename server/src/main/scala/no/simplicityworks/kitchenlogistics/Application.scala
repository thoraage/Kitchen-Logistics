package no.simplicityworks.kitchenlogistics

import org.eclipse.jetty.util.resource.Resource

object Application extends App {

  object Stack extends RestPlanModule with DatabaseModule with H2DatabaseProfileModule

  val http = unfiltered.jetty.Http(5000)
  http.current.setBaseResource(Resource.newResource(getClass.getResource("/public").getFile, false))
  Stack.plans.foldLeft(http)((http, plan) => http.plan(plan)).run()

}
