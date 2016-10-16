package no.simplicityworks.kitchenlogistics

import android.content.Intent
import android.os.Bundle
import android.view.{Menu, MenuItem, Window}
import org.scaloid.common._

import scala.concurrent.{Promise, Future}
import scala.util.{Try, Random}

class MainActivity extends GuiContext {

    val authenticationRequestCode = Random.nextInt()

    var app = new OperationsImplModule
            with DrawerMenuImplModule
            with KitLogRestStorageModule
            with ZXingScannerModule
            with DialogsModule
            with SelectingAuthenticationModule
            with SimpleSynchronizedActivityIntentBrokerModule {

            override def guiContext = MainActivity.this

            override def storageConfiguration = new StorageConfiguration {
                override lazy val hostAddress =
//                    "https://kitlog.herokuapp.com"
                    "http://192.168.0.102:8080"
//                    "http://192.168.1.206:8080"
                override lazy val authenticator = new Authenticator {
                    override def headers(wwwAuthenticate: Option[String]) = authentication.headers(wwwAuthenticate)
                }
            }
        }

    override def onActivityResult(requestCode: Int, resultCode: Int, data: Intent): Unit = {
        app.activityIntentBroker.addActivityResult(requestCode, resultCode, data)
    }

    override def futureOnUiThread[T](f: => T): Future[T] = {
        val promise = Promise[T]()
        runOnUiThread {
            promise.complete(Try(f))
        }
        promise.future
    }


    lazy val logContext = getClass.getSimpleName

    override def onOptionsItemSelected(item: MenuItem): Boolean = {
        item.getItemId match {
            case R.id.actionBarSearch =>
                app.operations.searchItems()
                true
            case R.id.action_bar_scan_new =>
                app.operations.scanNewItem()
                true
            case R.id.action_bar_scan_remove =>
                app.operations.scanRemoveItem()
                true
            case R.id.action_bar_new_item_group =>
                app.operations.createNewItemGroup()
                true
            case R.id.action_bar_rename_item_group =>
                app.operations.renameItemGroupName()
                true
            case R.id.action_bar_remove_item_group =>
                app.operations.removeItemGroupName()
                true
            case _ =>
                super.onOptionsItemSelected(item)
        }
    }

    override def onCreate(bundle: Bundle) {
        super.onCreate(bundle)
        requestWindowFeature(Window.FEATURE_ACTION_BAR)
        setContentView(R.layout.main)
        app.operations.initiate()
    }

    override def onCreateOptionsMenu(menu: Menu): Boolean = {
        val inflater = getMenuInflater
        inflater.inflate(R.menu.action_bar, menu)
        app.drawerMenu.setMenu(menu)
        super.onCreateOptionsMenu(menu)
    }

}
