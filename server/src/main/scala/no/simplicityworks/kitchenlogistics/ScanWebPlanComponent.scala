package no.simplicityworks.kitchenlogistics

import unfiltered.filter.Plan
import unfiltered.request._
import unfiltered.response._
import unfiltered.jetty.Server
import net.liftweb.json.Serialization._
import javax.servlet.http.HttpSession
import org.eclipse.jetty.server.session.SessionHandler
import unfiltered.Cookie
import unfiltered.response.Redirect
import unfiltered.Cookie
import unfiltered.response.ResponseString

trait ScanWebPlanComponent extends WebPlanComponent {

  override def registerWebPlan = ((server: Server) => server.context("/scan") {
    _.filter(new ScanServicePlan {})
  }) :: super.registerWebPlan

  trait ScanServicePlan extends Plan {

    val key = "scannedCodes"

    override def intent = {

      case req@GET(Path(Seg("scan" :: "codes" :: code :: Nil))) & Cookies(cookies) =>
        SetCookies(cookies(key).map(c => Cookie(key, c.value + "," + code)).getOrElse(Cookie(key, code))) ~> Redirect("/index.html")

      case GET(Path(Seg("scan" :: "codes" :: Nil))) & Cookies(cookies) =>
        ResponseString(write(cookies(key).toList.flatMap(_.value.split(','))))

    }
  }


}
