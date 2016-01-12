package no.simplicityworks.kitchenlogistics

import java.security.MessageDigest
import java.util.Date

import com.mchange.v2.c3p0.ComboPooledDataSource

import org.flywaydb.core.Flyway

import scala.slick.driver.JdbcDriver.simple._
import scala.slick.lifted.Tag

trait DatabaseModule extends DatabaseProfileModule {

    implicit val JavaUtilDateMapper =
        MappedColumnType.base[java.util.Date, java.sql.Timestamp] (
            d => new java.sql.Timestamp(d.getTime),
            d => new java.util.Date(d.getTime))

    object Products {
        val query = TableQuery[Products]

        def essentiallySameQuery(product: Product) =
            query.filter(p => p.id =!= product.id && p.code === product.code && p.name === product.name)

        def findByCode(code: String) =
            database withSession { implicit session: Session =>
                query.filter(_.code === code).list
            }

        def insert(product: Product): Int =
            database withSession { implicit session: Session =>
                query returning query.map(_.id) += product
            }
    }

    class Products(tag: Tag) extends Table[Product](tag, "global_product") {
        def id = column[Int]("id", O.PrimaryKey, O.AutoInc)

        def code = column[String]("code")

        def name = column[String]("name")

        def created = column[Date]("created")

        def * = (id.?, code, name, created) <>(Product.tupled, Product.unapply)
    }

    object ItemGroups {
        val query = TableQuery[ItemGroups]

        def update(itemGroup: ItemGroup): Unit = database withSession { implicit s =>
            query.filter(group => group.id === itemGroup.id).update(itemGroup).run
        }

        def isOwner(itemGroup: ItemGroup, user: User): Boolean = database withSession { implicit s =>
            query.filter(group => group.id === itemGroup.id && group.userId === user.id).length.run > 0
        }

        def getForUser(user: User) = database withSession { implicit s =>
            query.filter(_.userId === user.id).sortBy(_.name).list
        }

        def insert(itemGroup: ItemGroup): Int = database withSession { implicit s =>
            query returning query.map(_.id) += itemGroup
        }
    }


    class ItemGroups(tag: Tag) extends Table[ItemGroup](tag, "user_item_group") {
        def id = column[Int]("id", O.PrimaryKey, O.AutoInc)

        def userId = column[Int]("userId")

        def name = column[String]("name")

        def created = column[Date]("created")

        def user = foreignKey("item_group_user_fk", userId, TableQuery[Users])(_.id)

        def * = (id.?, userId.?, name, created) <>(ItemGroup.tupled, ItemGroup.unapply)

    }

    object Items {
        val query = TableQuery[Items]

        def insert(item: Item): Int = database withSession { implicit session: Session =>
            query returning query.map(_.id) += item
        }

        def delete(id: Int): Int = database withSession { implicit session: Session =>
            query.filter(_.id === id).delete
        }
    }

    class Items(tag: Tag) extends Table[Item](tag, "user_item") {
        def id = column[Int]("id", O.PrimaryKey, O.AutoInc)

        def userId = column[Int]("user_id")

        def productId = column[Int]("product_id")

        def itemGroupId = column[Int]("item_group_id")

        def created = column[Date]("created")

        def user = foreignKey("item_user_fk", userId, TableQuery[Users])(_.id)

        def product = foreignKey("item_product_fk", productId, TableQuery[Products])(_.id)

        def itemGroup = foreignKey("item_item_group_fk", itemGroupId, TableQuery[ItemGroups])(_.id)

        def * = (id.?, userId.?, productId, itemGroupId, created) <>(Item.tupled, Item.unapply)

        def all = database withSession { implicit session: Session =>
            TableQuery[Items].list
        }
    }

    object Users {
        val passwordSalt = "kitlogsaltysalt"
        val md5 = MessageDigest.getInstance("MD5")
        val query = TableQuery[Users]

        def apply(username: String): Option[User] = database withSession { implicit session =>
            query.filter(_.username === username).list.headOption
        }

        def saltPassword(s: String) = md5.digest((Users.passwordSalt + "pass").getBytes("UTF-8"))

        def insert(user: User): Int = database withSession { implicit session: Session =>
            query returning query.map(_.id) += user
        }
    }

    class Users(tag: Tag) extends Table[User](tag, "user") {
        def id = column[Int]("id", O.PrimaryKey, O.AutoInc)

        def username = column[String]("username")

        def email = column[String]("email")

        def created = column[Date]("created")

        def password = column[Array[Byte]]("password")

        def * = (id.?, username, email, password, created) <>(User.tupled, User.unapply)

    }

    lazy val database = {
        val ds = new ComboPooledDataSource()
        ds.setJdbcUrl(databaseProfile.jdbcUrl)
        ds.setDriverClass(databaseProfile.driverClass)
        ds.setUser(databaseProfile.username)
        ds.setPassword(databaseProfile.password)
        if (databaseProfile.generation == DatabaseGeneration.flyway) {
            val flyway = new Flyway
            flyway.setDataSource(ds)
            flyway.migrate()
        }
        Database.forDataSource(ds)
    }
    val databaseDdls = Seq(TableQuery[Products].ddl, TableQuery[Users].ddl, TableQuery[ItemGroups].ddl, TableQuery[Items].ddl)

}

