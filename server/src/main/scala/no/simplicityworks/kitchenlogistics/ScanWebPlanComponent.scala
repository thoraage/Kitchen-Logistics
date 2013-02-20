package no.simplicityworks.kitchenlogistics

import unfiltered.filter.Plan
import unfiltered.request._
import unfiltered.response._
import unfiltered.jetty.Server
import net.liftweb.json.Serialization._
import unfiltered.response.Redirect
import unfiltered.Cookie
import unfiltered.response.ResponseString

trait ScanWebPlanComponent extends WebPlanComponent {

  override def registerWebPlan = ((server: Server) => server.context("/scan") {
    _.filter(new ScanServicePlan {})
  }) :: super.registerWebPlan

  trait ScanServicePlan extends Plan {

    override def intent = {

      case req@GET(Path(Seg("scan" :: "codes" :: code :: Nil))) => {
        Redirect("/index.html?identifyScanCode=" + code.trim)
      }

    }
  }


}
