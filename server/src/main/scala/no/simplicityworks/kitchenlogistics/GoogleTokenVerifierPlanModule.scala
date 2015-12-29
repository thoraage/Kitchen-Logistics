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
    def verify(token: String): Option[VerificationResult]
}

case class VerificationResult(issued_to: String, audience: String, user_id: String, scope: String, expires_in: Int, access_type: String)

trait GoogleTokenVerifierPlanImplModule extends GoogleTokenVerifierPlanModule {

    def googleTokenVerifier = GoogleTokenVerifierImpl

    object GoogleTokenVerifierImpl extends GoogleTokenVerifier {
        private implicit val formats = Serialization.formats(NoTypeHints)

        override def verify(token: String): Option[VerificationResult] = {
            val a = Try(HttpConnection("https://www.googleapis.com").get(s"/oauth2/v1/tokeninfo?access_token=$token"))
            println(s"Token found: $a")
            a match {
                case Success(result) =>
                    Some(read[VerificationResult](result))
                case Failure(StatusCodeException(_, 400, _)) =>
                    Logger.getGlobal.info(s"Unable to validate token $token")
                    None
                case Failure(e) => throw e
            }
        }
    }
}
