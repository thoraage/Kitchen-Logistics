package no.simplicityworks.kitchenlogistics

import org.eclipse.jetty.util.resource.Resource

object Application extends App{
  val http = unfiltered.jetty.Http(1337)
  http.current.setBaseResource(Resource.newResource(getClass.getResource("/public").getFile, false))
      //.resources(getClass.getResource("/public"))
      //.context("/") { ctx: ContextBuilder => ctx.resources(getClass.getResource("/public")) }
  http.plan(Plans.TestPlan).run()
}
