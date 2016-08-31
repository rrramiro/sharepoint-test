package com.jw.sharepoint.examples

import java.util.Properties

import org.scalatest.FunSuite
import com.microsoft.sharepoint.ws.ListsSoap12Bindings

import scala.concurrent.Await
import scala.language.postfixOps
import scalaxb.SoapClientsAsync
import scala.concurrent.duration._
import scala.xml.NodeSeq

class SharePointListTest extends FunSuite {
  test("list"){
    val remote = new ListsSoap12Bindings with SoapClientsAsync with DispatchHttpAuthClientsAsync {
      val properties = new Properties{
        load(getClass.getClassLoader.getResourceAsStream("SharePointListExample.properties"))
      }
      val user = properties.getProperty("username")
      val password = properties.getProperty("password")
      val hostname = "infopoint"
    }
    val f = remote.service.getListItems(Some(remote.properties.getProperty("folder")), Some(""), None, None, Some(""), None, Some(""))

    val result = Await.result(f, 2 minutes)
    result.GetListItemsResult.foreach(_.mixed.foreach(_.value match {
      case node: NodeSeq => (node \ "data" \ "row").foreach( row => println(row \@ "ows_Document"))
      case _ =>
    }))
  }
}
