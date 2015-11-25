package no.simplicityworks.kitchenlogistics

import android.accounts.{AccountManager, Account}
import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.{Log, Base64}
import android.view.{Menu, Window, MenuItem}
import com.google.android.gms.auth.{UserRecoverableAuthException, GoogleAuthUtil}
import com.google.android.gms.auth.api.Auth
import com.google.android.gms.common.Scopes
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.common.api.GoogleApiClient.Builder
import com.google.android.gms.plus.Plus
import org.scaloid.common._

import scala.concurrent.duration.Duration
import scala.concurrent.{Future, Await, Promise}
import scala.util.{Success, Random}
import scala.concurrent.ExecutionContext.Implicits.global

class MainActivity extends SActivity with TypedFindView {

    val authenticationRequestCode = Random.nextInt()

    var app =
        new OperationsImplModule
            with KitLogRestStorageModule
            with MockDialogScannerModule
            with DialogsModule
            with SelectingAuthenticationModule
            with SimpleSynchronizedActivityIntentBrokerModule {
            override def guiContext = MainActivity.this

            override def storageConfiguration = new StorageConfiguration {
                private val ipAddress = "192.168.0.195"
                override lazy val hostAddress = "http://" + ipAddress + ":8080"
                override lazy val authenticator = new Authenticator {
                    override def headers(wwwAuthenticate: Option[String]) = authentication.headers(wwwAuthenticate)
                }
            }
        }

    override def onActivityResult(requestCode: Int, resultCode: Int, data: Intent): Unit = {
        app.activityIntentBroker.addActivityResult(requestCode, resultCode, data)
    }

    lazy val logContext = getClass.getSimpleName

    override def onOptionsItemSelected(item: MenuItem): Boolean = {
        item.getItemId match {
//            case R.id.actionBarSearch =>
//                //search
//                true
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
        app.operations.populateDrawerMenu()
    }

    override def onCreateOptionsMenu(menu: Menu): Boolean = {
        val inflater = getMenuInflater
        inflater.inflate(R.menu.action_bar, menu)
        app.operations.setMenu(menu)
        super.onCreateOptionsMenu(menu)
    }

}
