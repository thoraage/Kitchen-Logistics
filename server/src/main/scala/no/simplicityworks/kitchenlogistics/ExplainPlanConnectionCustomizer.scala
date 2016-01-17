package no.simplicityworks.kitchenlogistics

import java.sql.Connection

import com.mchange.v2.c3p0.AbstractConnectionCustomizer
import no.simplicityworks.kitchenlogistics.ResourceManagement._

class ExplainPlanConnectionCustomizer extends AbstractConnectionCustomizer {
    override def onAcquire(c: Connection, parentDataSourceIdentityToken: String) {
        using(c.createStatement())(_.execute("LOAD 'auto_explain'"))
        using(c.createStatement())(_.execute("SET auto_explain.log_min_duration = 0"))
    }
}
