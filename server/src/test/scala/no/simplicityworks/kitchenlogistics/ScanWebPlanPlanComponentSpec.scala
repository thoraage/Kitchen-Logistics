package no.simplicityworks.kitchenlogistics

import org.scalaquery.session._
import org.scalaquery.ql.basic.BasicDriver.Implicit._
import org.specs.Specification
import dispatch.{StatusCode, Http}
import unfiltered.spec.jetty.Served
import org.eclipse.jetty.server.session.SessionHandler

class ScanWebPlanPlanComponentSpec extends Specification with Served {

  val plan = new ScanWebPlanComponent {}

  def setup = server => {
    plan.registerWebPlan.foldLeft(server)((s, f) => f(s))
  }

  "The ScanWebPlan" should {
    "have no scans on session originally" in {
      val result = Http(host / "scan/codes/" as_str)
      result must_== "[]"
    }

    "adding scans puts scans on session" in {
      Http(host / "scan/codes/88" as_str) must throwA[StatusCode]
      Http(host / "scan/codes/" as_str) must_== """["88"]"""
      Http(host / "scan/codes/88" as_str) must throwA[StatusCode]
      Http(host / "scan/codes/" as_str) must_== """["88","88"]"""
    }
  }

}