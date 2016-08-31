package com.jw.sharepoint.examples

import com.ning.http.client.Realm.{AuthScheme, RealmBuilder}
import scalaxb.HttpClientsAsync
import scala.concurrent._, duration._

trait DispatchHttpAuthClientsAsync extends HttpClientsAsync{
  val user: String
  val password: String
  val hostname: String
  val requestTimeout = 60 seconds
  val connectionTimeout = 5 seconds

  lazy val httpClient = new HttpClient {
    import dispatch._, Defaults._
    lazy val http = Http.configure {
      _.setRequestTimeout(requestTimeout.toMillis.toInt)
      .setConnectTimeout(connectionTimeout.toMillis.toInt)
      .setFollowRedirect(true)
    }

    def request(in: String, address: java.net.URI, headers: Map[String, String]): Future[String] = {
      val (domain, principal) = user.split('\\').toList match {
        case d :: p :: Nil => d -> p
        case _ => "" -> user
      }

      val realm = new RealmBuilder()
        .setScheme(AuthScheme.NTLM)
        .setPrincipal(principal)
        .setNtlmDomain(domain)
        .setPassword(password)
        .setNtlmHost(hostname)
        .build()

      val req = url(address.toString).setBodyEncoding("UTF-8").setRealm(realm) <:< headers << in

      http(req > as.String)
    }
  }

}
