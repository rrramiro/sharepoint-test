package com.jw.sharepoint.examples

import java.util.Properties
import com.ning.http.client.Realm.{AuthScheme, RealmBuilder}
import scala.concurrent.duration.Duration
import scalaxb.HttpClientsAsync
import scala.concurrent._, duration._

trait DispatchHttpAuthClientsAsync extends HttpClientsAsync{
  val properties: Properties

  lazy val httpClient = new DispatchHttpClient {}

  def requestTimeout: Duration = 60.seconds
  def connectionTimeout: Duration = 5.seconds

  trait DispatchHttpClient extends HttpClient {
    import dispatch._, Defaults._
    lazy val http = Http.configure(_.
      setRequestTimeoutInMs(requestTimeout.toMillis.toInt).
      setConnectionTimeoutInMs(connectionTimeout.toMillis.toInt))

    def request(in: String, address: java.net.URI, headers: Map[String, String]): concurrent.Future[String] = {
      val realm = new RealmBuilder()
        .setScheme(AuthScheme.DIGEST)
        .setPrincipal(properties.getProperty("username"))
        .setPassword(properties.getProperty("password"))
        .build()

      val req = url(address.toString).setBodyEncoding("UTF-8").setRealm(realm) <:< headers << in
      http(req > as.String)
    }
  }

}
