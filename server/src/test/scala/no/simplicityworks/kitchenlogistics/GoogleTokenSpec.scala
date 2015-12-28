package no.simplicityworks.kitchenlogistics

import com.migcomponents.migbase64.Base64
import org.apache.http.HttpHeaders
import org.scalatest.{FeatureSpec, GivenWhenThen}

import scala.slick.driver.JdbcDriver.simple._

class GoogleTokenSpec extends FeatureSpec with GivenWhenThen with SpecBase {

    var verificationResultF: String => Option[VerificationResult] = { _ => None }

    override lazy val app = new RestPlanModule
        with InMemoryDatabaseModule
        with GoogleTokenAuthenticationPlanModule {

        override def googleTokenVerifier = new GoogleTokenVerifier {
            override def verify(token: String) = verificationResultF(token)
        }
    }

    def assertUnauthorized(f: => Any) {
        try {
            val result = f
            fail(s"Received: $result")
        } catch {
            case StatusCodeException(_, status, _) =>
                assert(status === 401)
        }
    }

    def googleTokenClient = new KitLogRestStorageModule {
        override lazy val storageConfiguration = new StorageConfiguration {
            override lazy val hostAddress = s"http://127.0.0.1:$port"
            override lazy val authenticator = new Authenticator {
                override def headers(wwwAuthenticate: Option[String]): Map[String, String] =
                    Map(HttpHeaders.AUTHORIZATION -> ("GoogleToken " + Base64.encodeToString("token".getBytes, false)))
            }
        }
    }

    feature("Item group get all") {
        scenario("Ok") {
            app.database withSession { implicit session =>
                app.ItemGroups.query.insert(ItemGroup(None, thoredge.id, "Kjøleskap"))
            }
            verificationResultF = { token =>
                if (token != "token") sys.error("Expected token")
                Some(VerificationResult("", "", "thoraageeldby@gmail.com", "", 3600, ""))
            }
            assert(await(googleTokenClient.storage.getItemGroups).size !== 0)
        }
        scenario("Unvalidated") {
            verificationResultF = { token =>
                None
            }
            assertUnauthorized(await(googleTokenClient.storage.getItemGroups))
        }
        scenario("New user") {
            verificationResultF = { token =>
                Some(VerificationResult("", "", "newguy@new.com", "", 3600, ""))
            }
            assert(await(googleTokenClient.storage.getItemGroups).size === 0)
            val count = app.database withSession { implicit session =>
                app.Users.query.filter(_.email === "newguy@new.com").length.run
            }

            assert(count === 1)
        }
    }


}
