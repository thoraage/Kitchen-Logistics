package no.simplicityworks.kitchenlogistics

import java.io.{IOException, InputStream}
import java.net.{HttpURLConnection, URL}
import scala.collection.mutable.{Map => MutableMap}
import com.migcomponents.migbase64.Base64

import scala.io.Source
import scala.util.{Success, Failure, Try}

trait Authenticator {
    def headers: Map[String, String]
}

object NoAuthenticator extends Authenticator {
    override lazy val headers = Map[String, String]()
}

case class BasicAuthenticator(username: String, password: String, encoding: String = "UTF-8") extends Authenticator {
    override lazy val headers = Map("Authorization" -> ("Basic " + Base64.encodeToString(s"$username:$password".getBytes(encoding), false)))
}

case class HttpConnection(baseUrl: String, authenticator: Authenticator = NoAuthenticator, headers: Map[String, String] = Map(), var cookies: MutableMap[String, String] = MutableMap()) {
    def accept(contentType: ContentType) =
        copy(headers = headers + ("Accept" -> contentType.contentType))

    def contentType(contentType: ContentType) =
        copy(headers = headers + ("Content-Type" -> contentType.contentType))

    def authenticator(authenticator: Authenticator) =
        copy(authenticator = authenticator)

    def get(path: String, encoding: String = "UTF-8"): String = {
        withConnection(path, "GET", doInput = true, doOutput = false) { connection =>
            readString(connection.getInputStream, encoding)
        }
    }

    def put(path: String, outContent: String, encoding: String = "UTF-8"): String = {
        withConnection(path, "PUT", doInput = true, doOutput = true) { connection =>
            connection.getOutputStream.write(outContent.getBytes(encoding))
            readString(connection.getInputStream, encoding)
        }
    }

    def delete(path: String, encoding: String = "UTF-8") {
        withConnection(path, "DELETE", doInput = true, doOutput = false) { connection =>
            readString(connection.getInputStream, encoding)
        }
    }

    protected def withConnection[T](path: String, method: String, doInput: Boolean, doOutput: Boolean, authenticating: Boolean = false)(doWith: HttpURLConnection => T): T  = {
        val connection = new URL(s"$baseUrl$path").openConnection().asInstanceOf[HttpURLConnection]
        connection.setRequestMethod(method)
        (headers ++ cookies).foreach(header => connection.setRequestProperty(header._1, header._2))
        connection.setDoInput(doInput)
        connection.setDoOutput(doOutput)
        connection.setInstanceFollowRedirects(false)
        val attempt: Try[T] = Try(doWith(connection)).recoverWith {
            case e: IOException =>
                val status = connection.getResponseCode
                Failure(StatusCodeException(s"Unexpected status code $status", status))
        } match {
            case success @ Success(_) =>
                val status = connection.getResponseCode
                if (status == HttpStatus.EMPTY || status == HttpStatus.OK) success
                else Failure(StatusCodeException(s"Unexpected status code $status", status))
            case f => f
        }
        val cookie = Option(connection.getHeaderField("Set-Cookie"))
        cookie.foreach(cookie => cookies += ("Cookie" -> cookie.replaceAll(";.*", "")))
        attempt.recover {
            case StatusCodeException(_, HttpStatus.UNAUTHORIZED, _) if !authenticating && authenticator != NoAuthenticator =>
                copy(headers = headers ++ authenticator.headers).withConnection(path, method, doInput, doOutput, authenticating = true)(doWith)
            case StatusCodeException(_, HttpStatus.MOVED_TEMPORARILY, _) =>
                withConnection(path, method, doInput, doOutput, authenticating = false)(doWith)
            case exception =>
                throw exception
        }.get
    }

    protected def readString(stream: InputStream, encoding: String) = Source.fromInputStream(stream).mkString

}

object HttpStatus {
    val OK = 200
    val EMPTY = 204
    val UNAUTHORIZED = 401
    val MOVED_TEMPORARILY = 302
}

object ContentType {
    lazy val json = ContentType("application/json")
}

case class ContentType(contentType: String)