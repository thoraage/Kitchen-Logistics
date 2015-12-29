package no.simplicityworks.kitchenlogistics

import android.accounts.Account
import android.app.Activity
import android.util.{Base64, Log}
import com.google.android.gms.auth.api.Auth
import com.google.android.gms.auth.{GoogleAuthUtil, UserRecoverableAuthException}
import com.google.android.gms.common.api.GoogleApiClient.Builder
import com.google.android.gms.plus.Plus
import org.scaloid.common.WidgetHelpers

import scala.concurrent.Await
import scala.concurrent.duration.Duration

trait AuthenticationModule {

    def authentication: AuthenticationService

}

trait AuthenticationService {

    def headers(wwwAuthenticate: Option[String]): Map[String, String]

}

trait SelectingAuthenticationModule extends AuthenticationModule with GuiContextModule with ActivityIntentBrokerModule {

    lazy val authentication = new AuthenticationService {
        val BasicAuth = "(?i)basic.*".r
        val GoogleTokenAuth = "(?i)googletoken.*".r

        def headers(wwwAuthenticate: Option[String]): Map[String, String] = {
            wwwAuthenticate match {
                case Some(BasicAuth()) => BasicAuthenticator("thoredge", "pass").headers(wwwAuthenticate)
                case Some(GoogleTokenAuth()) => GoogleTokenAuthenticator.headers(wwwAuthenticate)
                case _ => sys.error(s"Unknown www authenticate header: $wwwAuthenticate")
            }
        }
    }

    private object GoogleTokenAuthenticator extends Authenticator {
        override def headers(wwwAuthenticate: Option[String]): Map[String, String] = {
            try {
                Log.i(logContext, "Startet login")
                // TODO: Trenger
                val googleApiClient = new Builder(guiContext).addApi(Auth.CREDENTIALS_API).addApi(Plus.API).addScope(Plus.SCOPE_PLUS_LOGIN).build()
                Log.i(logContext, "Prøver connect")
                val result = googleApiClient.blockingConnect()
                if (!result.isSuccess) {
                    Log.i(logContext, s"DOH, better try to get auth ${result.getErrorMessage}")
//                    val promise = Promise[(Int, Intent)]()
                    val future = activityIntentBroker.getResponseOn(result.startResolutionForResult(guiContext, _))
                        //                    activityResultPromises = activityResultPromises + (authenticationRequestCode -> promise)
//                    result.startResolutionForResult(guiContext, authenticationRequestCode)
                    Await.result(future, Duration.Inf) match {
                        case (Activity.RESULT_OK, _) =>
                            Log.i(logContext, "Got positive result")
                            headers(wwwAuthenticate)
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
                    val token = GoogleAuthUtil.getToken(guiContext, account, scopes)
                    Log.i(logContext, s"##### Token: $token")
                    WidgetHelpers.toast(s"Token is $token")
                    val encodedString = Base64.encodeToString(token.getBytes, Base64.NO_WRAP)
                    Log.i(logContext, s"##### Encoded token: $encodedString")
                    Map("Authorization" -> s"GoogleToken $encodedString")
                }
            } catch {
                case e: UserRecoverableAuthException =>
                    Log.i(logContext, s"${e.getMessage}")
                    val future = activityIntentBroker.getResponseOn(guiContext.startActivityForResult(e.getIntent, _))
//                        startActivityForResult(e.getIntent, authenticationRequestCode)
//                    val promise = Promise[(Int, Intent)]()
                    Await.result(future, Duration.Inf) match {
                        case (Activity.RESULT_OK, _) =>
                            Log.i(logContext, "Got positive result")
                            headers(wwwAuthenticate)
                        case (resultCode, _) => sys.error(s"Intent result code $resultCode")
                    }
                //GooglePlayServicesAvailabilityException. This is a specific type of UserRecoverableAuthException indicating that the user's current version of Google Play services is outdated. Although the recommendation above for UserRecoverableAuthException also works for this exception, calling startActivityForResult() will immediately send users to Google Play Store to install an update, which may be confusing. So you should instead call getConnectionStatusCode() and pass the result to GooglePlayServicesUtil.getErrorDialog(). This returns a Dialog that includes an appropriate message and a button to take users to Google Play Store so they can install an update.
            }

        }
    }

    lazy val logContext = getClass.getSimpleName

}
