package no.simplicityworks.kitchenlogistics

import java.util.UUID

import unfiltered.request.{Cookies, HttpRequest}

import scala.slick.driver.JdbcDriver.simple._

trait SessionHandlerModule extends DatabaseModule {

    def sessionHandler = SessionHandler

    object SessionHandler {
        // TODO cache should empty itself
        val sessionCache = collection.mutable.Map[UUID, String]()

        def getAuthenticatedUsername(uuid: String) = sessionCache.get(UUID.fromString(uuid))

        object AuthenticatedUsername {
            def unapply[T](req: HttpRequest[T]): Option[String] =
                for {
                    cookieMap <- Cookies.unapply(req)
                    authCookie <- cookieMap("auth")
                    username <- sessionCache.get(UUID.fromString(authCookie.value))
                } yield username
        }

        object AuthenticatedUser {
            def unapply[T](req: HttpRequest[T]): Option[User] =
                database withSession { implicit session: Session =>
                    for {
                        uuid <- AuthenticatedUsername.unapply(req)
                        user <- (for {user <- TableQuery[Users] if user.username === uuid} yield user).firstOption
                    } yield user
                }
        }
   }

}
