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

trait SimpleSynchronizedActivityIntentBrokerModule extends ActivityIntentBrokerModule {

    override lazy val activityIntentBroker = new ActivityIntentBrokerService {
        // TODO Unanswered old promises should be purged
        private val activityResultPromises = mutable.Map[Int, Promise[(Int, Intent)]]()
        private val counter = new AtomicInteger(4711)

        override def getResponseOn(intentSender: (Int) => Unit): Future[(Int, Intent)] = {
            val requestCode = counter.incrementAndGet()
            val promise = Promise[(Int, Intent)]()
            activityResultPromises.synchronized {
                activityResultPromises.put(requestCode, promise)
            }
            intentSender(requestCode)
            promise.future
        }

        override def addActivityResult(requestCode: Int, resultCode: Int, intent: Intent) {
            activityResultPromises.synchronized {
                activityResultPromises.get(requestCode).foreach { promise =>
                    Log.i(logContext, s"Found promise which is currently: isCompleted = ${promise.isCompleted}")
                    activityResultPromises.remove(requestCode)
                    promise.complete(Success((resultCode, intent)))
                }
            }
        }

        lazy val logContext = getClass.getSimpleName

    }

}