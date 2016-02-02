package com.jw.sharepoint.examples

import java.io.File
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Properties
import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory
import org.w3c.dom.Element
import org.w3c.dom.Node
import org.w3c.dom.NodeList
import com.microsoft.sharepoint.webservices._
import com.microsoft.sharepoint.webservices.UpdateListItems.Updates
import com.microsoft.sharepoint.webservices.UpdateListItemsResponse.UpdateListItemsResult

object SharePointDeleteListItemExample {
  private val logger: Log = LogFactory.getLog(classOf[SharePointUploadDocumentExample])
  private val properties: Properties = new Properties

  /**
    * @param args
    */
  def main(args: Array[String]) {
    logger.debug("main...")
    val example: SharePointDeleteListItemExample = new SharePointDeleteListItemExample
    try {
      example.initialize
      val cp: CopySoap = example.getCopySoap
      example.uploadDocument(cp, properties.getProperty("copy.sourceFile"))
      val ls: ListsSoap = example.getListsSoap
      example.executeQueryAndDelete(ls)
    }
    catch {
      case ex: Exception => {
        logger.error("Error caught in main: ", ex)
      }
    }
  }
}

class SharePointDeleteListItemExample extends SharePointBaseExample {
  private var delete: String = null
  private var deleteListItemQuery: String = null
  private var queryOptions: String = null

  def getProperties: Properties = {
    return SharePointDeleteListItemExample.properties
  }

  @throws(classOf[Exception])
  def executeQueryAndDelete(ls: ListsSoap) {
    val today: Date = Calendar.getInstance.getTime
    val simpleDateFormat: SimpleDateFormat = new SimpleDateFormat("yyyy-MM-dd")
    val formattedDate: String = simpleDateFormat.format(today)
    val queryFormatted: String = String.format(deleteListItemQuery, formattedDate)
    val msQueryOptions: GetListItems.QueryOptions = new GetListItems.QueryOptions
    val msQuery: GetListItems.Query = new GetListItems.Query
    msQuery.getContent.add(SharePointBaseExample.createSharePointCAMLNode(queryFormatted))
    msQueryOptions.getContent.add(SharePointBaseExample.createSharePointCAMLNode(this.queryOptions))
    val result: GetListItemsResponse.GetListItemsResult = ls.getListItems(SharePointDeleteListItemExample.properties.getProperty("folder"), "", msQuery, null, "", msQueryOptions, "")
    SharePointBaseExample.writeResult(result.getContent.get(0), System.out)
    val element: Element = result.getContent.get(0).asInstanceOf[Element]
    val nl: NodeList = element.getElementsByTagName("z:row")

      var i: Int = 0
      while (i < nl.getLength) {
        {
          val node: Node = nl.item(i)
          val id: String = node.getAttributes.getNamedItem("ows_ID").getNodeValue
          val fileRefRelativePath: String = node.getAttributes.getNamedItem("ows_FileRef").getNodeValue
          SharePointDeleteListItemExample.logger.debug("id: " + id)
          SharePointDeleteListItemExample.logger.debug("fileRefRelativePath: " + fileRefRelativePath)
          val fileRef: String = SharePointDeleteListItemExample.properties.getProperty("delete.FileRef.base") + fileRefRelativePath.split("#")(1)
          SharePointDeleteListItemExample.logger.debug("fileRef: " + fileRef)
          deleteListItem(ls, SharePointDeleteListItemExample.properties.getProperty("folder"), id, fileRef)
        }
        {
          i += 1;
          i - 1
        }
      }

  }

  @throws(classOf[Exception])
  def deleteListItem(ls: ListsSoap, listName: String, listId: String, fileRef: String) {
    val deleteFormatted: String = String.format(delete, listId, fileRef)
    val u: UpdateListItems.Updates = new UpdateListItems.Updates
    u.getContent.add(SharePointBaseExample.createSharePointCAMLNode(deleteFormatted))
    val ret: UpdateListItemsResponse.UpdateListItemsResult = ls.updateListItems(listName, u)
    SharePointBaseExample.writeResult(ret.getContent.get(0), System.out)
  }

  @throws(classOf[Exception])
  override def initialize {
    SharePointDeleteListItemExample.logger.info("initialize()...")
    SharePointDeleteListItemExample.properties.load(getClass.getResourceAsStream("/SharePointDeleteListItemExample.properties"))
    super.initialize
    this.delete = new String(SharePointBaseExample.readAll(new File(this.getClass.getResource("/Delete.xml").toURI)))
    this.deleteListItemQuery = new String(SharePointBaseExample.readAll(new File(this.getClass.getResource("/DeleteListItemQuery.xml").toURI)))
    this.queryOptions = new String(SharePointBaseExample.readAll(new File(this.getClass.getResource("/QueryOptions.xml").toURI)))
  }
}
