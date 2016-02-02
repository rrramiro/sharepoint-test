package com.jw.sharepoint.examples

import java.io.File
import java.util.Properties
import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory
import org.w3c.dom.Element
import org.w3c.dom.Node
import org.w3c.dom.NodeList
import com.microsoft.sharepoint.webservices.GetListItems
import com.microsoft.sharepoint.webservices.GetListItemsResponse
import com.microsoft.sharepoint.webservices.ListsSoap

object SharePointListExample extends App {
  private val properties: Properties = new Properties
  private val logger: Log = LogFactory.getLog(classOf[SharePointListExample])

  logger.debug("main...")
  val example: SharePointListExample = new SharePointListExample
  try {
    example.initialize
    val ls: ListsSoap = example.getListsSoap
    example.querySharePointFolder(ls)
  }
  catch {
    case ex: Exception => {
      logger.error("Error caught in main: ", ex)
    }
  }
}

class SharePointListExample extends SharePointBaseExample {
  private var query: String = null
  private var queryOptions: String = null


  def getProperties: Properties = {
    return SharePointListExample.properties
  }

  @throws(classOf[Exception])
  def querySharePointFolder(ls: ListsSoap) {
    val viewFields: GetListItems.ViewFields = null
    val msQueryOptions: GetListItems.QueryOptions = new GetListItems.QueryOptions
    val msQuery: GetListItems.Query = new GetListItems.Query
    msQuery.getContent.add(SharePointBaseExample.createSharePointCAMLNode(query))
    msQueryOptions.getContent.add(SharePointBaseExample.createSharePointCAMLNode(this.queryOptions))
    val result: GetListItemsResponse.GetListItemsResult = ls.getListItems(SharePointListExample.properties.getProperty("folder"), "", msQuery, viewFields, "", msQueryOptions, "")
    SharePointBaseExample.writeResult(result.getContent.get(0), System.out)
    val element: Element = result.getContent.get(0).asInstanceOf[Element]
    val nl: NodeList = element.getElementsByTagName("z:row")

      var i: Int = 0
      while (i < nl.getLength) {
        {
          val node: Node = nl.item(i)
          SharePointListExample.logger.debug("ID: " + node.getAttributes.getNamedItem("ows_ID").getNodeValue)
          SharePointListExample.logger.debug("FileRef: " + node.getAttributes.getNamedItem("ows_FileRef").getNodeValue)
        }
        {
          i += 1;
          i - 1
        }
      }

  }

  @throws(classOf[Exception])
  protected override def initialize {
    SharePointListExample.properties.load(getClass.getResourceAsStream("/SharePointListExample.properties"))
    super.initialize
    this.query = new String(SharePointBaseExample.readAll(new File(this.getClass.getResource("/Query2.xml").toURI)))
    this.queryOptions = new String(SharePointBaseExample.readAll(new File(this.getClass.getResource("/QueryOptions2.xml").toURI)))
  }
}
