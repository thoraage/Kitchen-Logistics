package no.simplicityworks.kitchenlogistics

trait GoogleTokenVerifierPlanModule {

    def googleTokenVerifier: GoogleTokenVerifier

}

trait GoogleTokenVerifier {
    def verify(token: String): Option[VerificationResult]
}

case class VerificationResult(issued_to: String, audience: String, user_id: String, scope: String, expires_in: Int, access_type: String)
