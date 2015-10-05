package no.simplicityworks.kitchenlogistics

import java.io.InputStream
import java.net.{HttpURLConnection, URL}

import com.migcomponents.migbase64.Base64

import scala.io.Source

case class HttpConnection(url: String, headers: Map[String, String] = Map()) {
    def accept(contentType: ContentType) =
        this.copy(headers = headers + ("Accept" -> contentType.contentType))

    def contentType(contentType: ContentType) =
        this.copy(headers = headers + ("Content-Type" -> contentType.contentType))

    def basicAuth(username: String, password: String, encoding: String = "UTF-8") =
        this.copy(headers = headers + ("Authorization" -> ("Basic " + Base64.encodeToString(s"$username:$password".getBytes(encoding), false))))

    def get(path: String, encoding: String = "UTF-8"): String = {
        val connection = new URL(s"$url$path").openConnection().asInstanceOf[HttpURLConnection]
        connection.setRequestMethod("GET")
        headers.foreach(header => connection.setRequestProperty(header._1, header._2))
        connection.setDoInput(true)
        connection.setDoOutput(false)
        connection.setInstanceFollowRedirects(false)
        val status = connection.getResponseCode
        if (status != 200) {
            val cookie = Option(connection.getHeaderField("Set-Cookie"))
            cookie
                .map(cookie => copy(headers = headers + ("Cookie" -> cookie.replaceAll(";.*", ""))).get(path, encoding))
                .getOrElse(sys.error(s"Expected code 200, got $status: ${connection.getContent.asInstanceOf[String]}"))
        } else {
            readString(connection.getInputStream, encoding)
        }
    }

    def put(url: String, content: String, encoding: String = "UTF-8"): String = {
        val connection = new URL(s"$url").openConnection().asInstanceOf[HttpURLConnection]
        connection.setRequestMethod("PUT")
        headers.foreach(header => connection.setRequestProperty(header._1, header._2))
        connection.setDoInput(true)
        connection.setDoOutput(true)
        connection.setInstanceFollowRedirects(false)
        connection.getOutputStream.write(content.getBytes(encoding))
        val status = connection.getResponseCode
        if (status != 200) {
            val cookie = Option(connection.getHeaderField("Set-Cookie"))
            cookie
                .map(cookie => copy(headers = headers + ("Cookie" -> cookie.replaceAll(";.*", ""))).put(url, content, encoding))
                .getOrElse(sys.error(s"Expected code 200, got $status: ${connection.getContent.asInstanceOf[String]}"))
        } else {
            readString(connection.getInputStream, encoding)
        }
    }

    def readString(stream: InputStream, encoding: String) = Source.fromInputStream(stream).mkString

}

object ContentType {
    lazy val json = ContentType("application/json")
}

case class ContentType(contentType: String)