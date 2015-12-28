package no.simplicityworks.kitchenlogistics

import java.util.{UUID, Date}

import org.eclipse.jetty.http.HttpHeaders
import unfiltered.Cookie
import unfiltered.filter.Planify
import unfiltered.request._
import unfiltered.response._

import scala.slick.driver.JdbcDriver.simple._

trait GoogleTokenAuthenticationPlanModule extends AuthenticationPlanModule with SessionHandlerModule with GoogleTokenVerifierPlanModule with DatabaseModule {

    private val AuthenticatedUsername = sessionHandler.AuthenticatedUserId
    private val AuthenticatedUser = sessionHandler.AuthenticatedUser

    def authenticationPlan = Planify {
        case Path(Seg("rest" :: _)) & AuthenticatedUserId(_) =>
            Pass
        case Path(path) & GoogleTokenAuth(token) =>
            val result = googleTokenVerifier.verify(token)
            result.map { result =>
                database withSession { implicit session =>
                    val user = Users.query.filter(_.email === result.user_id).firstOption
                    if (user.isEmpty) {
                        Users.insert(User(None, result.user_id, result.user_id, Array(), new Date))
                    }
                }
                val uuid = UUID.randomUUID
                synchronized(sessionHandler.sessionCache += (uuid -> result.user_id))
                SetCookies(Cookie("auth", uuid.toString, maxAge = Some(1000 * 60 * 30))) ~> Redirect(path)
            }.getOrElse(unauthorized)
        case req@Path(Seg("rest" :: _)) =>
            val a = req.headers(HttpHeaders.AUTHORIZATION)
            unauthorized
    }

    private def unauthorized = Unauthorized ~> ResponseHeader("WWW-Authenticate", "GoogleToken realm=\"kitlog\"" :: Nil)
}
