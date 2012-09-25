package no.simplicityworks.kitchenlogistics

import net.liftweb.json.DefaultFormats
import unfiltered.jetty.Server

trait WebPlanComponent {

  implicit val formats = DefaultFormats

  def registerWebPlan: List[Server => Server] = Nil

}