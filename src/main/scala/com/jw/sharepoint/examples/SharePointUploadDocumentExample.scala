package com.jw.sharepoint.examples

import java.util.Properties
import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory
import com.microsoft.sharepoint.webservices._

object SharePointUploadDocumentExample {
  private val properties: Properties = new Properties
  private val logger: Log = LogFactory.getLog(classOf[SharePointUploadDocumentExample])

  /**
    * @param args
    */
  def main(args: Array[String]) {
    logger.debug("main...")
    try {
      val example: SharePointUploadDocumentExample = new SharePointUploadDocumentExample
      example.initialize
      val p: CopySoap = example.getCopySoap
      example.uploadDocument(p, properties.getProperty("copy.sourceFile"))
    }
    catch {
      case ex: Exception => {
        logger.error("Error caught in main: ", ex)
      }
    }
  }
}

class SharePointUploadDocumentExample extends SharePointBaseExample {
  def getProperties: Properties = {
    return SharePointUploadDocumentExample.properties
  }

  @throws(classOf[Exception])
  protected override def initialize {
    SharePointUploadDocumentExample.logger.info("initialize()...")
    SharePointUploadDocumentExample.properties.load(getClass.getResourceAsStream("/SharePointUploadDocumentExample.properties"))
    super.initialize
  }
}
