package no.simplicityworks.kitchenlogistics

import java.util.logging.Logger

import org.json4s.NoTypeHints
import org.json4s.jackson.Serialization
import org.json4s.jackson.Serialization._

import scala.util.{Failure, Success, Try}

trait GoogleTokenVerifierPlanModule {

    def googleTokenVerifier: GoogleTokenVerifier

}

trait GoogleTokenVerifier {
    def verify(token: String): Option[TokenInfo]
    def getUserInfo(token: String): Option[UserInfo]
}

case class TokenInfo(
    issued_to: Option[String],
    audience: Option[String],
    user_id: Option[String],
    scope: Option[String],
    expires_in: Option[Int],
    access_type: Option[String]
)

case class UserInfo(
   id: Option[String],
   email: Option[String],
   verified_email: Option[Boolean],
   name: Option[String],
   given_name: Option[String],
   family_name: Option[String],
   link: Option[String],
   picture: Option[String],
   gender: Option[String],
   locale: Option[String]
)

trait GoogleTokenVerifierPlanImplModule extends GoogleTokenVerifierPlanModule {

    def googleTokenVerifier = GoogleTokenVerifierImpl

    object GoogleTokenVerifierImpl extends GoogleTokenVerifier {
        private implicit val formats = Serialization.formats(NoTypeHints)

        override def verify(token: String): Option[TokenInfo] = {
            val a = Try(HttpConnection("https://www.googleapis.com").get(s"/oauth2/v1/tokeninfo?access_token=$token"))
            println(s"Access token $token")
            println(s"Token found: $a")
            a match {
                case Success(result) =>
                    Some(read[TokenInfo](result))
                case Failure(StatusCodeException(_, 400, _)) =>
                    Logger.getGlobal.info(s"Unable to validate token $token")
                    None
                case Failure(e) => throw e
            }
        }

        override def getUserInfo(token: String): Option[UserInfo] = {
            val a = Try(HttpConnection(s"https://www.googleapis.com").get(s"/oauth2/v1/userinfo?access_token=$token"))
            println(s"UserInfo found: $a")
            a match {
                case Success(result) =>
                    Some(read[UserInfo](result))
                case Failure(StatusCodeException(_, 400, _)) =>
                    Logger.getGlobal.info(s"Unable to retrieve user info for $token")
                    None
                case Failure(e) => throw e
            }
        }
    }
}
