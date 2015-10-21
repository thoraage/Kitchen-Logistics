package no.simplicityworks.kitchenlogistics

import java.io.{IOException, InputStream}
import java.net.{HttpURLConnection, URL}

import com.migcomponents.migbase64.Base64

import scala.io.Source

case class HttpConnection(baseUrl: String, headers: Map[String, String] = Map(), authenticationHeaders: Map[String, String] = Map()) {
    def accept(contentType: ContentType) =
        this.copy(headers = headers + ("Accept" -> contentType.contentType))

    def contentType(contentType: ContentType) =
        this.copy(headers = headers + ("Content-Type" -> contentType.contentType))

    def basicAuth(username: String, password: String, encoding: String = "UTF-8") =
        this.copy(authenticationHeaders = authenticationHeaders + ("Authorization" -> ("Basic " + Base64.encodeToString(s"$username:$password".getBytes(encoding), false))))

    def get(path: String, encoding: String = "UTF-8"): String = {
        withConnection(path, "GET", doInput = true, doOutput = false) { connection =>
            val status = connection.getResponseCode
            val content = readString(connection.getInputStream, encoding)
            if (status != HttpStatus.OK) {
                val cookie = Option(connection.getHeaderField("Set-Cookie"))
                cookie
                    .map(cookie => copy(headers = headers + ("Cookie" -> cookie.replaceAll(";.*", ""))).get(path, encoding))
                    .getOrElse(sys.error(s"Expected code 200, got $status: $content"))
            } else {
                content
            }
        }
    }

    def put(path: String, outContent: String, encoding: String = "UTF-8"): String = {
        withConnection(path, "PUT", doInput = true, doOutput = true) { connection =>
            connection.getOutputStream.write(outContent.getBytes(encoding))
            val status = connection.getResponseCode
            val inContent = readString(connection.getInputStream, encoding)
            if (status != HttpStatus.OK && status != HttpStatus.EMPTY) {
                val cookie = Option(connection.getHeaderField("Set-Cookie"))
                cookie
                    .map(cookie => copy(headers = headers + ("Cookie" -> cookie.replaceAll(";.*", ""))).put(path, outContent, encoding))
                    .getOrElse(throw StatusCodeException(s"Expected code 200, got $status: $inContent", status))
            } else {
                inContent
            }
        }
    }

    def delete(path: String, encoding: String = "UTF-8") {
        withConnection(path, "DELETE", doInput = true, doOutput = false) { connection =>
            val status = connection.getResponseCode
            val content = readString(connection.getInputStream, encoding)
            if (status != HttpStatus.EMPTY) {
                val cookie = Option(connection.getHeaderField("Set-Cookie"))
                cookie.foreach(cookie => copy(headers = headers + ("Cookie" -> cookie.replaceAll(";.*", ""))).delete(path, encoding))
                if (cookie.isEmpty) StatusCodeException(s"Expected code 200, got $status: $content", status)
            }
        }
    }

    protected def withConnection[T](path: String, method: String, doInput: Boolean, doOutput: Boolean, authenticating: Boolean = false)(f: HttpURLConnection => T): T  = {
        val connection = new URL(s"$baseUrl$path").openConnection().asInstanceOf[HttpURLConnection]
        connection.setRequestMethod(method)
        headers.foreach(header => connection.setRequestProperty(header._1, header._2))
        connection.setDoInput(doInput)
        connection.setDoOutput(doOutput)
        connection.setInstanceFollowRedirects(false)
        try catchStatusCodeExceptions(f(connection)) catch {
            case StatusCodeException(_, 401, _) if !authenticating =>
                copy(headers = headers ++ authenticationHeaders).withConnection(path, method, doInput, doOutput, authenticating = true)(f)
        }
    }

    protected def readString(stream: InputStream, encoding: String) = Source.fromInputStream(stream).mkString

    protected def catchStatusCodeExceptions[T](f: => T): T =
        try {
            f
        } catch {
            case e: IOException =>
                val statusRegex = """.*response code: (\d+).*""".r
                e.getMessage match {
                    case statusRegex(IntString(status)) =>
                        throw StatusCodeException(s"Expected code 20x, got $status", status)
                    case _ =>
                        throw e
                }
        }

}

object HttpStatus {
    val OK = 200
    val EMPTY = 204
}

object ContentType {
    lazy val json = ContentType("application/json")
}

case class ContentType(contentType: String)