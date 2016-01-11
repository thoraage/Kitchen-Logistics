package no.simplicityworks.kitchenlogistics

import java.util.concurrent.atomic.AtomicInteger

import android.content.Intent
import android.util.Log

import scala.collection.mutable
import scala.concurrent.{Future, Promise}
import scala.ref.WeakReference
import scala.util.Success

trait ActivityIntentBrokerModule {

    def activityIntentBroker: ActivityIntentBrokerService

}

trait ActivityIntentBrokerService {

    def getResponseOn(intentSender: Int => Unit): Future[(Int, Intent)]

    def addActivityResult(requestCode: Int, resultCode: Int, intent: Intent)

}

trait SimpleSynchronizedActivityIntentBrokerModule extends ActivityIntentBrokerModule with StableValuesModule {

    override lazy val activityIntentBroker = new ActivityIntentBrokerService {

        override def getResponseOn(intentSender: (Int) => Unit): Future[(Int, Intent)] = {
            val requestCode = stableValues.intentRequestIdCounter.incrementAndGet()
            val promise = Promise[(Int, Intent)]()
            stableValues.activityResultPromises.synchronized {
                stableValues.activityResultPromises.put(requestCode, promise)
            }
            intentSender(requestCode)
            promise.future
        }

        override def addActivityResult(requestCode: Int, resultCode: Int, intent: Intent) {
            stableValues.activityResultPromises.synchronized {
                stableValues.activityResultPromises.get(requestCode).foreach { promise =>
                    Log.i(logContext, s"Found promise which is currently: isCompleted = ${promise.isCompleted}")
                    stableValues.activityResultPromises.remove(requestCode)
                    promise.complete(Success((resultCode, intent)))
                }
            }
        }

        lazy val logContext = getClass.getSimpleName

    }

}