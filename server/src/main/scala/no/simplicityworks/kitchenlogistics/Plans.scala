package no.simplicityworks.kitchenlogistics

import unfiltered.filter.Plan
import unfiltered.request._
import unfiltered.response._
import scala.io.Source

object Plans {

  object TestPlan extends Plan{
    def intent = {
      case GET(Path(Seg("product" :: "rest" :: Nil))) =>
        Ok ~> ResponseString(Source.fromInputStream(getClass.getResourceAsStream("/public/index.html")).mkString)
    }
  }

}
