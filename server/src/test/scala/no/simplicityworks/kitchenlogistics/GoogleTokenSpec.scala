package no.simplicityworks.kitchenlogistics

import com.migcomponents.migbase64.Base64
import org.apache.http.HttpHeaders
import org.scalatest.{FeatureSpec, GivenWhenThen}

import scala.slick.driver.JdbcDriver.simple._

class GoogleTokenSpec extends FeatureSpec with GivenWhenThen with SpecBase {

    var verificationResultF: String => Option[TokenInfo] = { _ => None }
    var userInfoResultF: String => Option[UserInfo] = { _ => None }

    override lazy val app = new RestPlanModule
        with InMemoryDatabaseModule
        with GoogleTokenAuthenticationPlanModule {

        override def googleTokenVerifier = new GoogleTokenVerifier {
            override def verify(token: String) = verificationResultF(token)
            override def getUserInfo(token: String) = userInfoResultF(token)
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
        scenario("Ok, existing") {
            def countUsers = app.database withSession { implicit session => app.Users.query.length.run }
            thoredge
            val originalUserCount = countUsers
            app.database withSession { implicit session =>
                app.ItemGroups.query.insert(ItemGroup(None, thoredge.id, "Kjøleskap"))
            }
            verificationResultF = { token =>
                if (token != "token") sys.error("Expected token")
                Some(TokenInfo(Some(""), Some(""), Some("oitrewirte"), Some(""), Some(3600), Some("")))
            }
            userInfoResultF = { token =>
                if (token != "token") sys.error("Expected token")
                Some(UserInfo(Some("oitrewirte"), Some("thoraageeldby@gmail.com"), Some(true), None, None, None, None, None, None, None))
            }
            assert(await(googleTokenClient.storage.getItemGroups).size !== 0)
            assert(originalUserCount === countUsers)
        }
        scenario("Unvalidated") {
            verificationResultF = _ => None
            userInfoResultF = _ => None
            assertUnauthorized(await(googleTokenClient.storage.getItemGroups))
        }
        scenario("Ok, new user") {
            verificationResultF = _ => Some(TokenInfo(Some(""), Some(""), Some("fdkasjfkdsa"), Some(""), Some(3600), Some("")))
            userInfoResultF = _ => Some(UserInfo(Some("fdkasjfkdsa"), Some("newguy@new.com"), Some(true), None, None, None, None, None, None, None))
            assert(await(googleTokenClient.storage.getItemGroups).size === 0)
            val newUsers = app.database withSession { implicit session =>
                app.Users.query.filter(_.email === "newguy@new.com").list
            }

            assert(newUsers.size === 1)
            assert(newUsers.head.username === "fdkasjfkdsa")
        }
    }


}
