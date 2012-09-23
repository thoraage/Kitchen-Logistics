package no.simplicityworks.kitchenlogistics

import unfiltered.filter.Plan
import unfiltered.request._
import unfiltered.response.Redirect
import unfiltered.jetty.Server

trait ScanWebPlanComponent extends WebPlanComponent {

  override def registerWebPlan = ((server: Server) => server.context("/scan") {
    _.filter(new ScanServicePlan {})
  }) :: super.registerWebPlan

  trait ScanServicePlan extends Plan {

    override def intent = {

      case req@GET(Path(Seg("scan" :: "products" :: code :: Nil))) =>
        Redirect("/index.html")

    }
  }


}
