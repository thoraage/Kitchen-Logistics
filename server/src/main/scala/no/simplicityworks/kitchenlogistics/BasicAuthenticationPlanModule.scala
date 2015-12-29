package no.simplicityworks.kitchenlogistics

import java.security.MessageDigest
import java.util.UUID

import unfiltered.Cookie
import unfiltered.filter.Planify
import unfiltered.request._
import unfiltered.response._

import scala.slick.driver.JdbcDriver.simple._

trait BasicAuthenticationPlanModule extends AuthenticationPlanModule with DatabaseModule with SessionHandlerModule {

    private val md5 = MessageDigest.getInstance("MD5")

    private val AuthenticatedUserId = sessionHandler.AuthenticatedUserId
    private val AuthenticatedUser = sessionHandler.AuthenticatedUser

    override def authenticationPlan = Planify {
        case Path(Seg("rest" :: _)) & AuthenticatedUserId(_) =>
            Pass
        case Path(path) & BasicAuth(name, pass) =>
            val user = database withSession { implicit session: Session =>
                (for {user <- TableQuery[Users] if user.username === name} yield user).firstOption
            }
            if (user.exists(user => user.password.sameElements(md5.digest((Users.passwordSalt + pass).getBytes("UTF-8"))))) {
                val uuid = UUID.randomUUID
                synchronized(sessionHandler.sessionCache += (uuid -> user.get.email))
                SetCookies(Cookie("auth", uuid.toString, maxAge = Some(1000 * 60 * 30))) ~> Redirect(path)
            } else {
                Unauthorized ~> ResponseHeader("WWW-Authenticate", "Basic realm=\"kitlog\"" :: Nil)
            }
        case req@Path(Seg("rest" :: _)) =>
            Unauthorized ~> ResponseHeader("WWW-Authenticate", "Basic realm=\"kitlog\"" :: Nil)
    }

}
