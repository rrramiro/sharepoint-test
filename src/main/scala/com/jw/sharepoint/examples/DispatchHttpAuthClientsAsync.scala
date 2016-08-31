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
    lazy val http = Http.configure {
      _.setRequestTimeout(requestTimeout.toMillis.toInt)
      .setConnectTimeout(connectionTimeout.toMillis.toInt)
      .setFollowRedirect(true)
    }

    def request(in: String, address: java.net.URI, headers: Map[String, String]): Future[String] = {
      val user = properties.getProperty("username")
      val password = properties.getProperty("password")

      val (domain, principal) = user.split('\\').toList match {
        case d :: p :: Nil => d -> p
        case _ => "" -> user
      }

      val realm = new RealmBuilder()
        .setScheme(AuthScheme.NTLM)
        .setPrincipal(principal)
        .setNtlmDomain(domain)
        .setPassword(password)
        .setNtlmHost("infopoint")
        .build()

      val req: Req = url(address.toString).setBodyEncoding("UTF-8").setRealm(realm) <:< headers << in

      http(req > as.String)
    }
  }

}
