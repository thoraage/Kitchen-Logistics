package no.simplicityworks.kitchenlogistics

import scala.io.StdIn

object GoogleTokenTestApp extends App {

    val app = new GoogleTokenVerifierPlanImplModule {}

    println(app.googleTokenVerifier.verify(StdIn.readLine()))

}