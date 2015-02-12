package no.simplicityworks.kitchenlogistics

import java.security.MessageDigest

import com.mchange.v2.c3p0.ComboPooledDataSource
import java.sql.Date

import org.flywaydb.core.Flyway

trait DatabaseModule extends DatabaseProfileModule {

    import databaseProfile.driver.simple._

    object Products extends Table[Product]("global_product") {
        def id = column[Int]("id", O.PrimaryKey, O.AutoInc)

        def code = column[String]("code")

        def name = column[String]("name")

        def created = column[Date]("created")

        def * = id.? ~ code ~ name ~ created <>(Product, Product.unapply _)

        def forInsert = code ~ name ~ created <>( { t => Product(None, t._1, t._2, t._3)}, { (p: Product) => Some((p.code, p.name, p.created))})

        def findByCode(code: String) =
            database withSession { implicit session: Session =>
                Query(Products).where(_.code === code).list
            }

        def insert(product: Product): Int =
            database withSession { implicit session: Session =>
                forInsert returning id insert product
            }
    }

    object ItemGroups extends Table[ItemGroup]("user_item_group") {
        def id = column[Int]("id", O.PrimaryKey, O.AutoInc)

        def userId = column[Int]("userId")

        def name = column[String]("name")

        def created = column[Date]("created")

        def user = foreignKey("item_group_user_fk", userId, Users)(_.id)

        def * = id.? ~ userId.? ~ name ~ created <>(ItemGroup, ItemGroup.unapply _)

        def forInsert = userId.? ~ name ~ created <>( { t => ItemGroup(None, t._1, t._2, t._3)}, { (i: ItemGroup) => Some((i.userId, i.name, i.created))})

        def getAll = database withSession { implicit session: Session =>
            Query(ItemGroups).list
        }

        def insert(itemGroup: ItemGroup): Int = database withSession { implicit s: Session =>
            forInsert returning id insert itemGroup
        }
    }

    object Items extends Table[Item]("user_item") {
        def id = column[Int]("id", O.PrimaryKey, O.AutoInc)

        def userId = column[Int]("user_id")

        def productId = column[Int]("product_id")

        def itemGroupId = column[Int]("item_group_id")

        def created = column[Date]("created")

        def user = foreignKey("item_user_fk", userId, Users)(_.id)

        def product = foreignKey("item_product_fk", productId, Products)(_.id)

        def itemGroup = foreignKey("item_item_group_fk", itemGroupId, ItemGroups)(_.id)

        def * = id.? ~ userId.? ~ productId ~ itemGroupId ~ created <>(Item, Item.unapply _)

        def forInsert = userId.? ~ productId ~ itemGroupId ~ created <>( { t => Item(None, t._1, t._2, t._3, t._4)}, { (i: Item) => Some((i.userId, i.productId, i.itemGroupId, i.created))})

        def all = database withSession { implicit session: Session =>
            Query(Items).list
        }

        def insert(item: Item): Int = database withSession { implicit session: Session =>
            forInsert returning id insert item
        }

        def delete(id: Int): Int = database withSession { implicit session: Session =>
            Query(Items).where(_.id === id).delete
        }

    }

    object Users extends Table[User]("user") {
        val passwordSalt = "kitlogsaltysalt"

        def id = column[Int]("id", O.PrimaryKey, O.AutoInc)

        def username = column[String]("username")

        def email = column[String]("mail")

        def created = column[Date]("created")

        def password = column[Array[Byte]]("password")

        def * = id.? ~ username ~ email ~ password ~ created <>(User, User.unapply _)

        def forInsert = username ~ email ~ password ~ created <>( { t => User(None, t._1, t._2, t._3, t._4)}, { (i: User) => Some((i.username, i.email, i.password, i.created))})

        def insert(user: User): Int = database withSession { implicit session: Session =>
            forInsert returning id insert user
        }
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
    if (databaseProfile.generation == DatabaseGeneration.slickDdl) {
        database withSession { implicit session: Session =>
            val md5 = MessageDigest.getInstance("MD5")
            val ddls = Seq(Products.ddl, Users.ddl, ItemGroups.ddl, Items.ddl)
            ddls.map(_.createStatements.toList).foreach(println)
            ddls.foreach(_.create)
            Products.insertAll(
                Product(None, "5423", "Nexus S"),
                Product(None, "43123", "Motorola XOOM™ with Wi-Fi"),
                Product(None, "43728432", "ROLA XOOM™"))
            val userId = Users.insert(User(None, "thoredge", "thoraageeldby@gmail.com", md5.digest((Users.passwordSalt + "pass").getBytes("UTF-8"))))
            ItemGroups.insert(ItemGroup(None, Some(userId), "Kjøleskap"))
        }
    }

}

