package no.simplicityworks.kitchenlogistics

import android.accounts.Account
import android.app.Activity
import android.util.{Base64, Log}
import com.google.android.gms.auth.api.Auth
import com.google.android.gms.auth.{GoogleAuthUtil, UserRecoverableAuthException}
import com.google.android.gms.common.api.GoogleApiClient.Builder
import com.google.android.gms.plus.Plus
import org.scaloid.common.{WidgetHelpers, _}

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
                val googleApiClient = new Builder(guiContext).addApi(Auth.CREDENTIALS_API).addApi(Plus.API).addScope(Plus.SCOPE_PLUS_LOGIN).build()
                Log.i(logContext, "Prøver connect")
                val result = googleApiClient.blockingConnect()
                if (!result.isSuccess) {
                    Log.i(logContext, s"DOH, better try to get auth ${result.getErrorMessage}")
                    val future = activityIntentBroker.getResponseOn(result.startResolutionForResult(guiContext, _))
                    Await.result(future, Duration.Inf) match {
                        case (Activity.RESULT_OK, _) =>
                            Log.i(logContext, "Got positive result on auth request")
                            headers(wwwAuthenticate)
                        case (resultCode, _) =>
                            sys.error(s"Intent result code $resultCode")
                    }
                } else {
                    val accountName = Plus.AccountApi.getAccountName(googleApiClient)
                    Log.i(logContext, s"Account name = $accountName")
                    if (accountName == null) sys.error("Not logged into google account")
                    val account = new Account(accountName, GoogleAuthUtil.GOOGLE_ACCOUNT_TYPE)
                    val scopes = "oauth2:https://www.googleapis.com/auth/userinfo.profile https://www.googleapis.com/auth/userinfo.email"
                    val token = GoogleAuthUtil.getToken(guiContext, account, scopes)
                    WidgetHelpers.toast(R.string.autenticated.r2String)
                    val encodedString = Base64.encodeToString(token.getBytes, Base64.NO_WRAP)
                    Map("Authorization" -> s"GoogleToken $encodedString")
                }
            } catch {
                case e: UserRecoverableAuthException =>
                    Log.i(logContext, s"User recoverable auth exception: ${e.getMessage}")
                    val future = activityIntentBroker.getResponseOn(guiContext.startActivityForResult(e.getIntent, _))
                    Await.result(future, Duration.Inf) match {
                        case (Activity.RESULT_OK, _) =>
                            Log.i(logContext, "Got positive result on auth resolve")
                            headers(wwwAuthenticate)
                        case (resultCode, _) => sys.error(s"Intent result code $resultCode")
                    }
            }

        }
    }

    lazy val logContext = getClass.getSimpleName

}
