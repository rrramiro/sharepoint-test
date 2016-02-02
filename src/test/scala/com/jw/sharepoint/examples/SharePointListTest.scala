package com.jw.sharepoint.examples

import java.util.Properties

import org.scalatest.FunSuite
import com.microsoft.sharepoint.ws.{ListsSoap, ListsSoap12Bindings}
import com.microsoft.sharepoint.ws._

import scala.concurrent.Await
import scala.language.postfixOps
import scalaxb.{DispatchHttpClientsAsync, SoapClientsAsync}
import scala.concurrent.duration._

class SharePointListTest extends FunSuite {
  test("list"){
    val xmlQuery = <Query>
      <Where>
        <And>
          <BeginsWith>
            <FieldRef Name="Editor"/>
            <Value Type="Text">Ramiro</Value>
          </BeginsWith>
          <Contains>
            <FieldRef Name= "Editor"/>
            <Value Type= "Text">Ramiro</Value>
          </Contains>
        </And>
      </Where>
    </Query>
    val xmlQueryOptions = <QueryOptions>
      <IncludeMandatoryColumns>TRUE</IncludeMandatoryColumns>
      <ViewAttributes Scope="RecursiveAll"/>
      <DateInUtc>TRUE</DateInUtc>
    </QueryOptions>
    val remote = new ListsSoap12Bindings with SoapClientsAsync with DispatchHttpAuthClientsAsync {
      val properties = new Properties{
        load(getClass.getResourceAsStream("/SharePointListExample.properties"))
      }
    }
    val ls: ListsSoap = remote.service
    val msQuery = scalaxb.fromXML[Query](xmlQuery)
    val msQueryOptions = None//Some(scalaxb.fromXML[QueryOptions](xmlQueryOptions))
    val result = ls.getListItems(Some(remote.properties.getProperty("folder")), Some(""), Some(msQuery), None, Some(""), msQueryOptions, Some(""))

    println(result)

    Await.ready(result, 2 minutes).onComplete{ case r =>
      println(r)
    }

  }

}
