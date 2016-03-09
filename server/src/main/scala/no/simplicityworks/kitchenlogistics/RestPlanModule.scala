package no.simplicityworks.kitchenlogistics

import java.util.Date

import org.json4s.NoTypeHints
import org.json4s.jackson.Serialization
import org.json4s.jackson.Serialization.{read, write}
import unfiltered.directives.Directives._
import unfiltered.directives._
import unfiltered.filter.Planify
import unfiltered.request._
import unfiltered.response._

import scala.language.higherKinds
import scala.slick.driver.JdbcDriver.simple._

trait RestPlanModule extends PlanCollectionModule with DatabaseModule with SessionHandlerModule with AuthenticationPlanModule {

    override def plans = authenticationPlan :: authorizationPlan :: restPlan :: super.plans

    private val AuthenticatedUser = sessionHandler.AuthenticatedUser
    private implicit val formats = Serialization.formats(NoTypeHints)

    private def contentType(tpe: String) =
        when {
            case RequestContentType(`tpe`) =>
        } orElse UnsupportedMediaType ~> ResponseString("Content type supported: " + tpe)

//    private def extract: Params.Extract[Nothing, String] =
//        new Params.Extract("code", Params.first ~> Params.nonempty)

    case class ItemSummary(count: Int, product: Product, lastItemId: Int, itemGroupId: Int)

    implicit class PimpedQuery[T, U, V[_]](query: Query[T ,U, V]) {
        def takeConditional(limits: Seq[String]) = limits match {
            case Seq(IntString(limit), _*) => query.take(limit)
            case _ => query
        }
    }

    private val authorizationPlan = Planify {
        case Path(Seg("rest" :: "itemGroups" :: IntString(itemGroupId) :: Nil)) & AuthenticatedUser(user) =>
            database withSession { implicit session =>
                if (ItemGroups.query.filter(_.id === itemGroupId).list.forall(_.userId == user.id)) Pass
                else Forbidden
            }
        case Path(Seg("rest" :: "items" :: IntString(itemId) :: Nil)) & AuthenticatedUser(user) =>
            database withSession { implicit session =>
                if (Items.query.filter(_.id === itemId).list.forall(_.userId == user.id)) Pass
                else Forbidden
            }
    }

    private val restPlan = Planify {
        val default = LiteralColumn(1) === LiteralColumn(1)
        def isDefined(strings: Seq[String], restrict: Column[Boolean]) = if (strings.isEmpty) default else restrict

        Directive.Intent {
            case Path(Seg("rest" :: "products" :: Nil)) => {
                for {_ <- GET; _ <- Accepts.Json; code <- parameterValues("code")} yield
                    Ok ~> ResponseString(write(Products.findByCode(code.head)))
            } orElse {
                for {_ <- PUT; r <- request[Any]} yield {
                    val id = Products.insert(read[Product](Body string r))
                    Ok ~> ResponseString(write(Map("id" -> id)))
                }
            }

            case Path(Seg("rest" :: "products" :: IntString(productId) :: Nil)) & AuthenticatedUser(user) => {
                for {_ <- PUT; r <- request[Any]} yield {
                    database withSession { implicit session =>
                        val product = read[Product](Body string r).copy(id = Some(productId))
                        if (Items.query.filter(i => i.productId === productId && i.userId =!= user.id.get).length.run == 0) {
                            val sameProduct = Products.essentiallySameQuery(product).list
                            if (sameProduct.isEmpty) {
                                Products.query.filter(_.id === productId).update(product)
                            } else {
                                val first :: rest = (product :: sameProduct).sortBy(_.created)
                                val restIds = rest.flatMap(_.id)
                                Items.query.filter(_.productId inSet restIds).map(_.productId).update(first.id.get)
                                if (first.id == product.id) Products.query.filter(_.id === product.id).update(product)
                                Products.query.filter(_.id inSet restIds).delete
                            }
                        } else {
                            val newProductId = Products.insert(product.copy(id = None, created = new Date))
                            Items.query.filter(i => i.productId === productId && i.userId === user.id.get).map(_.productId).update(newProductId)
                        }
                    }
                    Ok ~> NoContent
                }
            } orElse {
                for {_ <- GET} yield {
                    database withSession { implicit session: Session =>
                        Products.query.filter(_.id === productId).list.headOption match {
                            case Some(product) =>
                                Ok ~> ResponseString(write(product))
                            case None =>
                                NotFound
                        }
                    }
                }
            }

            case Path(Seg("rest" :: "items" :: Nil)) & AuthenticatedUser(user) => {
                for {
                    _ <- GET; _ <- Accepts.Json; r <- request[Any]
                    itemGroups <- parameterValues("itemGroup")
                    codes <- parameterValues("code")
                    filters <- parameterValues("filter")
                    limit <- parameterValues("limit")
                    sortBy <- parameterValues("sortBy")
                } yield {
                    val items = (database withSession { implicit session: Session =>
                        val productItem = for {
                            item <- TableQuery[Items] if item.userId === user.id && isDefined(itemGroups, item.itemGroupId inSet itemGroups.map(_.toInt))
                            product <- item.product if isDefined(codes, product.code inSet codes) &&
                                filters.foldLeft(default)((query, filter) => query &&
                                product.name.toLowerCase.like(s"%${filter.toLowerCase}%"))
                        } yield (product, item)
                        sortBy.headOption match {
                            case Some("latest") =>
                                productItem.sortBy(_._2.id.desc).takeConditional(limit)
                                    .map { case (product, item) => (1, product, item.id?, item.itemGroupId)}.list
                            case _ =>
                                productItem.groupBy(p => (p._1, p._2.itemGroupId)).takeConditional(limit)
                                    .map { case ((product, itemGroupId), pair) => (pair.length, product, pair.map(_._2.id).max, itemGroupId) }.sortBy(_._2.name).list
                        }
                    }).map(p => ItemSummary(p._1, p._2, p._3.get, p._4))
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
                        Items.query.filter(_.id === itemId).list.headOption match {
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

            case Path(Seg("rest" :: "itemGroups" :: IntString(itemGroupId) :: Nil)) & AuthenticatedUser(user) => {
                database withSession { implicit s =>
                    for {_ <- GET } yield {
                        ItemGroups.query.filter(_.id === itemGroupId).list.headOption match {
                            case Some(itemGroup) => Ok ~> ResponseString(write(itemGroup))
                            case None => NotFound
                        }
                    }
                }
            } orElse {
                for {_ <- PUT; r <- request[Any]} yield {
                    val itemGroup = read[ItemGroup](Body string r).copy(id = Some(itemGroupId))
                    ItemGroups.update(itemGroup)
                    NoContent
                }
            } orElse {
                for {_ <- DELETE} yield {
                    database withSession { implicit s =>
                        TableQuery[ItemGroups].filter(_.id === itemGroupId).delete
                        Ok ~> NoContent
                    }
                }
            }
        }
    }

}
