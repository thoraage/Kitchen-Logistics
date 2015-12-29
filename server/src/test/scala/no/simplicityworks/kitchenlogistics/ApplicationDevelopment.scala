package no.simplicityworks.kitchenlogistics

import java.util.Date

import scala.slick.driver.JdbcDriver.simple._

object ApplicationDevelopment extends Application {

    lazy override val stack = new RestPlanModule
        with InMemoryDatabaseModule
        with GoogleTokenAuthenticationPlanModule
        with GoogleTokenVerifierPlanImplModule {

        database withSession { implicit session: Session =>
            databaseDdls.foreach(_.create)
            TableQuery[Products].insertAll(
                Product(None, "5423", "Nexus S"),
                Product(None, "43123", "Motorola XOOM™ with Wi-Fi"),
                Product(None, "43728432", "ROLA XOOM™"))
            val userId = Users.insert(new User(None, "thoredge", "thoraageeldby@gmail.com", Users.saltPassword("pass"), new Date))
            TableQuery[ItemGroups].insert(ItemGroup(None, Some(userId), "Kjøleskap"))
        }
    }

}
