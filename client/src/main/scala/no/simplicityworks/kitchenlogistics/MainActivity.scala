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

//    var projectId = "kitlog-1114"
    val authenticationRequestCode = Random.nextInt()
//    val clientId = "54175869382-1qpqrk8mmne6vvtlk0fdi0pbsuqlbb6a.apps.googleusercontent.com"

    var app: OperationsModule with StorageModule with ScannerModule with DialogsModule =
        new OperationsImplModule with KitLogRestStorageModule with MockDialogScannerModule with DialogsModule {
            override def guiContext = MainActivity.this

            override def storageConfiguration = new StorageConfiguration {
                private val ipAddress = "192.168.0.195"
                override lazy val hostAddress = "http://" + ipAddress + ":8080"
                override lazy val authenticator = new Authenticator {
                    override def headers = Map()//authentication.headers
                }
            }
        }

    var activityResultPromises = Map[Int, Promise[(Int, Intent)]]()

    override def onActivityResult(requestCode: Int, resultCode: Int, data: Intent): Unit = {
        Log.i(logContext, s"Reply from intent, result code $resultCode")
        activityResultPromises.get(requestCode).foreach { promise =>
            Log.i(logContext, s"Found promise which is currently: isCompleted = ${promise.isCompleted}")
            activityResultPromises = activityResultPromises - requestCode
            promise.complete(Success((resultCode, data)))
        }
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
        Log.i(logContext, "Starter login")
        Future(login()) onFailure {
            case result => Log.i(logContext, "Doh", result)
        }
    }

    def login() {
        try {
            Log.i(logContext, "Startet login")
            // TODO: Trenger
            val googleApiClient = new Builder(getApplicationContext).addApi(Auth.CREDENTIALS_API).addApi(Plus.API).addScope(Plus.SCOPE_PLUS_LOGIN).build()
            Log.i(logContext, "Prøver connect")
            val result = googleApiClient.blockingConnect()
            if (!result.isSuccess) {
                Log.i(logContext, s"DOH, better try to get auth ${result.getErrorMessage}")
                val promise = Promise[(Int, Intent)]()
                activityResultPromises = activityResultPromises + (authenticationRequestCode -> promise)
                result.startResolutionForResult(MainActivity.this, authenticationRequestCode)
                Await.result(promise.future, Duration.Inf) match {
                    case (Activity.RESULT_OK, _) =>
                        Log.i(logContext, "Got positive result")
                        login()
                    case (resultCode, _) => sys.error(s"Intent result code $resultCode")
                }
            } else {

                Log.i(logContext, s"#### Resolution is: $result")

                //                        val manager = AccountManager.get(MainActivity.this)
                //                        val list = manager.getAccounts

                //                        Log.i(getClass.getSimpleName, s"#### Fant antall: ${list.size}")
                //                        for (account <- list) {
                //                            if (account.`type`.equalsIgnoreCase("com.google")) {
                //                                Log.i(getClass.getSimpleName, s"#### Fant: ${account.name}")
                //                            }
                //                        }

                //list.map(_.name).headOption.orNull
                val accountName = Plus.AccountApi.getAccountName(googleApiClient)
                Log.i(logContext, s"Account name = $accountName")
                if (accountName == null) sys.error("Not logged into google account")
                val account = new Account(accountName, GoogleAuthUtil.GOOGLE_ACCOUNT_TYPE)
                val scopes = "oauth2:https://www.googleapis.com/auth/userinfo.profile"
                // "audience:server:client_id:" + clientId
                val token = GoogleAuthUtil.getToken(getApplicationContext, account, scopes)
                runOnUiThread(Log.i(logContext, s"##### Token: $token"))
                Map("Authorization" -> s"Token ${Base64.encodeToString(token.getBytes, Base64.DEFAULT)}")
                WidgetHelpers.toast(s"Token is $token")
            }
        } catch {
            case e: UserRecoverableAuthException =>
                Log.i(logContext, s"${e.getMessage}")
                startActivityForResult(e.getIntent, authenticationRequestCode)
                val promise = Promise[(Int, Intent)]()
                Await.result(promise.future, Duration.Inf) match {
                    case (Activity.RESULT_OK, _) =>
                        Log.i(logContext, "Got positive result")
                        login()
                    case (resultCode, _) => sys.error(s"Intent result code $resultCode")
                }
            //GooglePlayServicesAvailabilityException. This is a specific type of UserRecoverableAuthException indicating that the user's current version of Google Play services is outdated. Although the recommendation above for UserRecoverableAuthException also works for this exception, calling startActivityForResult() will immediately send users to Google Play Store to install an update, which may be confusing. So you should instead call getConnectionStatusCode() and pass the result to GooglePlayServicesUtil.getErrorDialog(). This returns a Dialog that includes an appropriate message and a button to take users to Google Play Store so they can install an update.
        }
    }

    override def onCreateOptionsMenu(menu: Menu): Boolean = {
        val inflater = getMenuInflater
        inflater.inflate(R.menu.action_bar, menu)
        app.operations.setMenu(menu)
        super.onCreateOptionsMenu(menu)
    }

}
