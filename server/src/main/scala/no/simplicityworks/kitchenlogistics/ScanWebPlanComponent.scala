package no.simplicityworks.kitchenlogistics

import unfiltered.filter.Plan
import unfiltered.request._
import unfiltered.response.Redirect
import unfiltered.jetty.Server

trait ScanWebPlanComponent extends WebPlanComponent with ThreadMountedScalaQuerySessionComponent {
  this: ProductDatabaseComponent =>

  override def registerWebPlan = ((server: Server) => server.context("/scan") {
    _.filter(new ScanServicePlan with ThreadMountedScalaQuerySession)
  }) :: super.registerWebPlan

  trait ScanServicePlan extends Plan with ScalaQuerySession{

    override def intent = {

      case req@GET(Path(Seg("scan" :: "products" :: code :: Nil))) =>
        Products.findByCode(code)//TODO: Now what? no-hit => save, single-hit => save, multi-hit => ?
        Redirect("/index.html")

    }
  }


}
