package no.simplicityworks.kitchenlogistics

import java.util.concurrent.atomic.AtomicInteger

import android.content.Intent

import scala.collection.mutable
import scala.concurrent.Promise

trait StableValuesModule extends StorageModule {

    lazy val stableValues = SingletonObjectStableValues

    object SingletonObjectStableValues {
        import SingletonObjectStableValueHolder._

        def itemGroups = map.getOrElse("itemGroups", Nil).asInstanceOf[List[ItemGroup]]
        def itemGroups_=(itemGroups: List[ItemGroup]) {
            map += "itemGroups" -> itemGroups
        }

        def selectedItemGroup = map.getOrElse("selectedItemGroup", None).asInstanceOf[Option[ItemGroup]]
        def selectedItemGroup_=(itemGroup: Option[ItemGroup]) {
            map += "selectedItemGroup" -> itemGroup
        }

        def dialogStickFunction = map.getOrElse("dialogStickFunction", None).asInstanceOf[Option[() => Unit]]
        def dialogStickFunction_=(f: Option[() => Unit]): Unit = {
            map += "dialogStickFunction" -> f
        }

        def intentRequestIdCounter = map.getOrElseUpdate("intentRequestIdCounter", new AtomicInteger(4711)).asInstanceOf[AtomicInteger]
        // TODO Unanswered old promises should be purged
        def activityResultPromises = map.getOrElseUpdate("activityResultPromises", mutable.Map[Int, Promise[(Int, Intent)]]()).asInstanceOf[mutable.Map[Int, Promise[(Int, Intent)]]]
    }
}

object SingletonObjectStableValueHolder {
    val map = mutable.Map[String, Any]()
}