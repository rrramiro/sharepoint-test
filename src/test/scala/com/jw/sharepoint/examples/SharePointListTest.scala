package com.jw.sharepoint.examples

import java.util.Properties

import org.scalatest.FunSuite
import com.microsoft.sharepoint.ws.ListsSoap12Bindings
import scala.concurrent.Await
import scala.language.postfixOps
import scalaxb.SoapClientsAsync
import scala.concurrent.duration._

class SharePointListTest extends FunSuite {
  test("list"){
    val remote = new ListsSoap12Bindings with SoapClientsAsync with DispatchHttpAuthClientsAsync {
      val properties = new Properties{
        load(getClass.getClassLoader.getResourceAsStream("SharePointListExample.properties"))
      }
    }
    val f = remote.service.getListItems(Some(remote.properties.getProperty("folder")), Some(""), None, None, Some(""), None, Some(""))

    val result = Await.result(f, 2 minutes)
    println(result)
  }

}
