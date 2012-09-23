package no.simplicityworks.kitchenlogistics

import net.liftweb.json.DefaultFormats
import org.scalaquery.ql.SimpleFunction
import unfiltered.jetty.Server

trait WebPlanComponent {

  implicit val formats = DefaultFormats
  val identityFunction = SimpleFunction.nullary[Int]("identity")

  def registerWebPlan: List[Server => Server] = Nil

}