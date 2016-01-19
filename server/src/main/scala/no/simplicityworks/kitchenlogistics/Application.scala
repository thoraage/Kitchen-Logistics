package no.simplicityworks.kitchenlogistics

import org.eclipse.jetty.util.resource.Resource

import scala.util.Properties

trait Application extends App {

    val stack: RestPlanModule

    val http = unfiltered.jetty.Http(Properties.envOrElse("PORT", "8080").toInt)
    http.current.setBaseResource(Resource.newResource(getClass.getResource("/public").toExternalForm, false))
    stack.database
    stack.plans.foldLeft(http)((http, plan) => http.plan(plan)).run()

}
