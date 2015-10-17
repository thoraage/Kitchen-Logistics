package no.simplicityworks.kitchenlogistics

import java.security.MessageDigest
import java.util.UUID

import org.json4s.NoTypeHints
import org.json4s.jackson.Serialization
import org.json4s.jackson.Serialization.{read, write}
import unfiltered.Cookie
import unfiltered.directives.Directives._
import unfiltered.directives._
import unfiltered.filter.Planify
import unfiltered.request._
import unfiltered.response._

import scala.slick.driver.JdbcDriver.simple._

trait RestPlanModule extends PlanCollectionModule with DatabaseModule {

    override def plans = authenticationPlan :: restPlan :: super.plans

    // TODO cache should empty itself
    private val sessionCache = collection.mutable.Map[UUID, String]()
    private val md5 = MessageDigest.getInstance("MD5")

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

    private val authenticationPlan = Planify {
        case Path(Seg("rest" :: "items" :: IntString(itemId) :: Nil)) & AuthenticatedUser(user) =>
            database withSession { implicit session =>
                if (Items.query.filter(_.id === itemId).list.forall(_.userId == user.id)) Pass
                else Forbidden
            }
        case Path(Seg("rest" :: _)) & AuthenticatedUsername(username) =>
            Pass
        case Path(path) & BasicAuth(name, pass) =>
            val user = database withSession { implicit session: Session =>
                (for {user <- TableQuery[Users] if user.username === name} yield user).firstOption
            }
            if (user.exists(user => user.password.sameElements(md5.digest((Users.passwordSalt + pass).getBytes("UTF-8"))))) {
                val uuid = UUID.randomUUID
                synchronized(sessionCache += (uuid -> name))
                SetCookies(Cookie("auth", uuid.toString, maxAge = Some(1000 * 60 * 30))) ~> Redirect(path)
            } else {
                Unauthorized ~> ResponseHeader("WWW-Authenticate", "Basic realm=\"kitlog\"" :: Nil)
            }
        case req@Path(Seg("rest" :: _)) =>
            Unauthorized ~> ResponseHeader("WWW-Authenticate", "Basic realm=\"kitlog\"" :: Nil)
    }

    private implicit val formats = Serialization.formats(NoTypeHints)

    private def contentType(tpe: String) =
        when {
            case RequestContentType(`tpe`) =>
        } orElse UnsupportedMediaType ~> ResponseString("Content type supported: " + tpe)

//    private def extract: Params.Extract[Nothing, String] =
//        new Params.Extract("code", Params.first ~> Params.nonempty)

    case class ItemSummary(count: Int, product: Product, lastItemId: Int)

    private val restPlan = unfiltered.filter.Planify {
        val default = LiteralColumn(1) === LiteralColumn(1)
        def isDefined(strings: Seq[String], restrict: Column[Boolean]) = if (strings.isEmpty) default else restrict

        Directive.Intent {
            case Path(Seg("rest" :: "products" :: Nil)) => {
                for {_ <- GET; _ <- Accepts.Json; code <- parameterValues("code")} yield
                    Ok ~> ResponseString(write(Products.findByCode(code.head))) // TODO will crash
            } orElse {
                for {_ <- PUT; r <- request[Any]} yield {
                    val id = Products.insert(read[Product](Body string r))
                    Ok ~> ResponseString(write(Map("id" -> id)))
                }
            }

            case Path(Seg("rest" :: "items" :: Nil)) & AuthenticatedUser(user) => {
                for {_ <- GET; _ <- Accepts.Json; r <- request[Any]; itemGroups <- parameterValues("itemGroup"); codes <- parameterValues("code")} yield {
                    val items = (database withSession { implicit session: Session =>
                        (for {
                            item <- TableQuery[Items] if item.userId === user.id && isDefined(itemGroups, item.itemGroupId inSet itemGroups.map(_.toInt))
                            product <- item.product if isDefined(codes, product.code inSet codes)
                        } yield (product, item))
                            .groupBy(p => p._1)
                            .map { case (product, pair) => (product, pair.length, pair.map(_._2.id).max)}.list
                    }).map(p => ItemSummary(p._2, p._1, p._3.get))
                    Ok ~> ResponseString(write(items))
                }
            } orElse {
                for {_ <- PUT; r <- request[Any]} yield {
                    database withSession { implicit session: Session =>
                        val id = Items.insert(read[Item](Body string r).copy(userId = user.id))
                        Ok ~> ResponseString(write(Map("id" -> id)))
                    }
                }
            }

            case Path(Seg("rest" :: "items" :: IntString(itemId) :: Nil)) & AuthenticatedUser(user) => {
                for {_ <- DELETE} yield {
                    database withSession { implicit session: Session =>
                        Items.delete(itemId)
                        Ok ~> NoContent
                    }
                }
            } orElse {
                database withSession { implicit session: Session =>
                    for {_ <- GET} yield {
                        TableQuery[Items].filter(_.id === itemId).list.headOption match {
                            case Some(item) =>
                                Ok ~> ResponseString(write(item))
                            case None =>
                                NotFound
                        }
                    }
                }
            } orElse {
                for {_ <- PUT; r <- request[Any]} yield {
                    database withSession { implicit session: Session =>
                        val updated = TableQuery[Items]
                            .filter(_.id === itemId)
                            .update(read[Item](Body string r).copy(id = Some(itemId), userId = user.id))
                        if (updated == 0) NotFound
                        else NoContent
                    }
                }
            }

            case Path(Seg("rest" :: "itemGroups" :: Nil)) & AuthenticatedUser(user) => {
                for {_ <- GET; _ <- Accepts.Json; r <- request[Any]} yield
                    Ok ~> ResponseString(write(ItemGroups.getForUser(user)))
            } orElse {
                for {_ <- PUT; r <- request[Any]} yield {
                    val id = ItemGroups.insert(read[ItemGroup](Body string r).copy(userId = user.id))
                    Ok ~> ResponseString(write(Map("id" -> id)))
                }
            }

            case Path(Seg("rest" :: "itemGroups" :: IntString(id) :: Nil)) & AuthenticatedUser(user) => {
                for {_ <- PUT; r <- request[Any]} yield {
                    val itemGroup = read[ItemGroup](Body string r).copy(id = Some(id))
                    if (!ItemGroups.isOwner(itemGroup, user))
                        Forbidden ~> ResponseString("Is not owner")
                    else {
                        ItemGroups.update(itemGroup)
                        NoContent
                    }
                }
            } orElse {
                for {_ <- DELETE} yield {
                    database withSession { implicit s =>
                        val itemGroup = TableQuery[ItemGroups].filter(_.id === id).list.head
                        if (!ItemGroups.isOwner(itemGroup, user))
                            Forbidden ~> ResponseString("Is not owner")
                        else {
                            TableQuery[ItemGroups].filter(_.id === id).delete
                            Ok ~> NoContent
                        }
                    }
                }
            }
        }
    }

}
