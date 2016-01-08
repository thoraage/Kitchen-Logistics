package no.simplicityworks.kitchenlogistics

import java.util.{UUID, Date}

import org.eclipse.jetty.http.HttpHeaders
import unfiltered.Cookie
import unfiltered.filter.Planify
import unfiltered.request._
import unfiltered.response._

import scala.slick.driver.JdbcDriver.simple._

trait GoogleTokenAuthenticationPlanModule extends AuthenticationPlanModule with SessionHandlerModule with GoogleTokenVerifierPlanModule with DatabaseModule {

    private val AuthenticatedUserId = sessionHandler.AuthenticatedUserId
    private val AuthenticatedUser = sessionHandler.AuthenticatedUser

    override def authenticationPlan = Planify {
        case Path(Seg("rest" :: _)) & AuthenticatedUserId(_) =>
            Pass
        case Path(path) & GoogleTokenAuth(token) =>
            val result = for {
                _ <- googleTokenVerifier.verify(token)
                userInfo <- googleTokenVerifier.getUserInfo(token)
                email <- userInfo.email
                userId <- userInfo.id
            } yield database withSession { implicit session =>
                val user = Users.query.filter(_.email === email).firstOption
                if (user.isEmpty) {
                    Users.insert(User(None, userId, email, Array(), new Date))
                }
                val uuid = UUID.randomUUID
                synchronized(sessionHandler.sessionCache += (uuid -> email))
                SetCookies(Cookie("auth", uuid.toString, maxAge = Some(1000 * 60 * 30))) ~> Redirect(path)
            }
            result.getOrElse(unauthorized)
        case req@Path(Seg("rest" :: _)) =>
            val a = req.headers(HttpHeaders.AUTHORIZATION)
            unauthorized
    }

    private def unauthorized = Unauthorized ~> ResponseHeader("WWW-Authenticate", "GoogleToken realm=\"kitlog\"" :: Nil)
}
