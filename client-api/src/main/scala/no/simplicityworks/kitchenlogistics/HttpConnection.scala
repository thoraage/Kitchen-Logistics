package no.simplicityworks.kitchenlogistics

import java.io.{StringReader, StringWriter}
import java.net.{HttpURLConnection, URL}

import org.apache.commons.codec.binary.Base64
import org.apache.commons.io.IOUtils

case class HttpConnection(url: String, headers: Map[String, String] = Map()) {
    def accept(contentType: ContentType) =
        this.copy(headers = headers + ("Accept" -> contentType.contentType))

    def contentType(contentType: ContentType) =
        this.copy(headers = headers + ("Content-Type" -> contentType.contentType))

    def basicAuth(username: String, password: String, encoding: String = "UTF-8") =
        this.copy(headers = headers + ("Authorization" -> ("Basic " + Base64.encodeBase64String(s"$username:$password".getBytes(encoding)))))

    def get(path: String, encoding: String = "UTF-8"): String = {
        val connection = new URL(s"$url$path").openConnection().asInstanceOf[HttpURLConnection]
        connection.setRequestMethod("GET")
        headers.foreach(header => connection.setRequestProperty(header._1, header._2))
        connection.setDoInput(true)
        connection.setDoOutput(false)
        connection.setInstanceFollowRedirects(false)
        val status = connection.getResponseCode
        if (status != 200) {
            import scala.collection.JavaConverters._
            val cookie = Option(connection.getHeaderField("Set-Cookie"))
            cookie
                .map(cookie => copy(headers = headers + ("Cookie" -> cookie.replaceAll(";.*", ""))).get(path, encoding))
                .getOrElse(sys.error(s"Expected code 200, got $status: ${connection.getContent.asInstanceOf[String]}"))
        } else {
            val writer = new StringWriter
            IOUtils.copy(connection.getInputStream, writer, encoding)
            writer.toString
        }
    }

    def put(url: String, content: String, encoding: String = "UTF-8"): String = {
        val connection = new URL(s"$url").openConnection().asInstanceOf[HttpURLConnection]
        connection.setRequestMethod("PUT")
        headers.foreach(header => connection.setRequestProperty(header._1, header._2))
        connection.setDoInput(true)
        connection.setDoOutput(true)
        connection.setInstanceFollowRedirects(false)
        IOUtils.copy(new StringReader(content), connection.getOutputStream, encoding)
        val status = connection.getResponseCode
        if (status != 200) {
            import scala.collection.JavaConverters._
            val cookie = Option(connection.getHeaderField("Set-Cookie"))
            cookie
                .map(cookie => copy(headers = headers + ("Cookie" -> cookie.replaceAll(";.*", ""))).put(url, content, encoding))
                .getOrElse(sys.error(s"Expected code 200, got $status: ${connection.getContent.asInstanceOf[String]}"))
        } else {
            val writer = new StringWriter
            IOUtils.copy(connection.getInputStream, writer, encoding)
            writer.toString
        }
    }

}

object ContentType {
    lazy val json = ContentType("application/json")
}

case class ContentType(contentType: String)