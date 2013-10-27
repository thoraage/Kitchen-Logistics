package no.simplicityworks.kitchenlogistics

import java.sql.Date

// TODO: This should really be a module, but json4s isn't coping to well with instantiation of class inside trait

case class Product(id: Option[Int], code: String, name: String, created: Date = Dates.now)

case class Item(id: Option[Int], userId: Int, productId: Int, itemGroupId: Int, created: Date = Dates.now)

case class ItemGroup(id: Option[Int], userId: Int, name: String, created: Date = Dates.now)

case class User(id: Option[Int], username: String, email: String, password: Array[Byte] = Array(), created: Date = Dates.now)

object Dates {
  def now = new Date(System.currentTimeMillis())
}
