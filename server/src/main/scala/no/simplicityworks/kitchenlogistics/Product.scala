package no.simplicityworks.kitchenlogistics

import java.util.Date

// TODO: This should really be a module, but json4s isn't coping to well with instantiation of class inside trait

case class Product(id: Option[Int], code: String, name: String, languageIso639_2: String, created: Date = new Date)

case class Item(id: Option[Int], userId: Option[Int], productId: Int, itemGroupId: Int, amount: Float, created: Date = new Date, updated: Date = new Date)

case class ItemGroup(id: Option[Int], userId: Option[Int], name: String, created: Date = new Date)

case class User(id: Option[Int], username: String, email: String, password: Array[Byte] = Array(), created: Date = new Date)
