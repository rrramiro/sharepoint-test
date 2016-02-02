package com.jw.sharepoint.examples

import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileInputStream
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.io.OutputStreamWriter
import java.io.StringReader
import java.net.Authenticator
import java.net.URL
import java.util.Properties
import javax.xml.namespace.QName
import javax.xml.parsers.DocumentBuilder
import javax.xml.parsers.DocumentBuilderFactory
import javax.xml.transform.OutputKeys
import javax.xml.transform.Transformer
import javax.xml.transform.TransformerFactory
import javax.xml.transform.dom.DOMSource
import javax.xml.transform.stream.StreamResult
import javax.xml.ws.BindingProvider
import javax.xml.ws.Holder
import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory
import org.w3c.dom.Document
import org.w3c.dom.Element
import org.w3c.dom.Node
import org.xml.sax.InputSource
import com.microsoft.sharepoint.webservices.Copy
import com.microsoft.sharepoint.webservices.CopyErrorCode
import com.microsoft.sharepoint.webservices.CopyResult
import com.microsoft.sharepoint.webservices.CopyResultCollection
import com.microsoft.sharepoint.webservices.CopySoap
import com.microsoft.sharepoint.webservices.DestinationUrlCollection
import com.microsoft.sharepoint.webservices.FieldInformation
import com.microsoft.sharepoint.webservices.FieldInformationCollection
import com.microsoft.sharepoint.webservices.FieldType
import com.microsoft.sharepoint.webservices.Lists
import com.microsoft.sharepoint.webservices.ListsSoap

object SharePointBaseExample {
  private val logger: Log = LogFactory.getLog(classOf[SharePointBaseExample])

  @throws(classOf[Exception])
  def createSharePointCAMLNode(theXML: String): Node = {
    logger.debug("createSharePointCAMLNode()...")
    logger.debug("CAML is: \n" + theXML)
    val documentBuilderFactory: DocumentBuilderFactory = DocumentBuilderFactory.newInstance
    documentBuilderFactory.setValidating(false)
    documentBuilderFactory.newDocumentBuilder.parse(new InputSource(new StringReader(theXML))).getDocumentElement
  }

  @throws(classOf[Exception])
  def writeResult(result: AnyRef, stream: OutputStream) {
    if (result == null) {
      logger.warn("result was null...")
      return
    }
    if (!result.isInstanceOf[Element]) {
      logger.warn("Not sure what to do with this response.  It should be Element, but was: " + result.getClass.getName)
      return
    }
    val e: Element = result.asInstanceOf[Element]
    val tf: TransformerFactory = TransformerFactory.newInstance
    val transformer: Transformer = tf.newTransformer
    transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "no")
    transformer.setOutputProperty(OutputKeys.METHOD, "xml")
    transformer.setOutputProperty(OutputKeys.INDENT, "yes")
    transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8")
    transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4")
    transformer.transform(new DOMSource(e.getOwnerDocument), new StreamResult(new OutputStreamWriter(stream, "UTF-8")))
  }

  @throws(classOf[IOException])
  def readAll(file: File): Array[Byte] = {
    logger.debug("readAll()..." + file.getAbsolutePath)
    var ous: ByteArrayOutputStream = null
    var ios: InputStream = null
    try {
      val buffer: Array[Byte] = new Array[Byte](4096)
      ous = new ByteArrayOutputStream
      ios = new FileInputStream(file)
      var read: Int = 0
      while ((({
        read = ios.read(buffer); read
      })) != -1) ous.write(buffer, 0, read)
    } finally {
      try {
        if (ous != null) ous.close()
      } finally {
        if (ios != null) ios.close()
      }
    }
    ous.toByteArray
  }
}

abstract class SharePointBaseExample {
  protected def getProperties: Properties

  @throws(classOf[Exception])
  protected def getListsSoap: ListsSoap = {
    SharePointBaseExample.logger.info("Creating a ListsSoap instance...")
    val service: Lists = new Lists(new URL(getProperties.getProperty("wsdl")), new QName("http://schemas.microsoft.com/sharepoint/soap/", "Lists"))
    val port: ListsSoap = service.getListsSoap
    val bp: BindingProvider = port.asInstanceOf[BindingProvider]
    bp.getRequestContext.put(BindingProvider.USERNAME_PROPERTY, getProperties.getProperty("username"))
    bp.getRequestContext.put(BindingProvider.PASSWORD_PROPERTY, getProperties.getProperty("password"))
    bp.getRequestContext.put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY, getProperties.getProperty("endpoint"))
    return port
  }

  @throws(classOf[Exception])
  protected def getCopySoap: CopySoap = {
    SharePointBaseExample.logger.info("Creating a CopySoap instance...")
    val service: Copy = new Copy(new URL(getProperties.getProperty("copy.wsdl")), new QName("http://schemas.microsoft.com/sharepoint/soap/", "Copy"))
    val copySoap: CopySoap = service.getCopySoap
    val bp: BindingProvider = copySoap.asInstanceOf[BindingProvider]
    bp.getRequestContext.put(BindingProvider.USERNAME_PROPERTY, getProperties.getProperty("username"))
    bp.getRequestContext.put(BindingProvider.PASSWORD_PROPERTY, getProperties.getProperty("password"))
    bp.getRequestContext.put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY, getProperties.getProperty("copy.endpoint"))
    return copySoap
  }

  @throws(classOf[Exception])
  protected def initialize {
    SharePointBaseExample.logger.debug("initialize()...")
    val cm: java.net.CookieManager = new java.net.CookieManager()
    java.net.CookieHandler.setDefault(cm)
    Authenticator.setDefault(new SharepointAuthenticator(getProperties))
  }

  @throws(classOf[Exception])
  protected def uploadDocument(port: CopySoap, sourceUrl: String) {
    val f: File = new File(sourceUrl)
    SharePointBaseExample.logger.debug("Uploading: " + f.getName)
    var url: String = getProperties.getProperty("copy.location") + f.getName
    val destinationUrlCollection: DestinationUrlCollection = new DestinationUrlCollection
    destinationUrlCollection.getString.add(url)
    if (getProperties.getProperty("copy.location2") != null) {
      url = getProperties.getProperty("copy.location2") + f.getName
      destinationUrlCollection.getString.add(url)
    }
    val titleFieldInformation: FieldInformation = new FieldInformation
    titleFieldInformation.setDisplayName("Title")
    titleFieldInformation.setType(FieldType.TEXT)
    titleFieldInformation.setValue(f.getName)
    val fields: FieldInformationCollection = new FieldInformationCollection
    fields.getFieldInformation.add(titleFieldInformation)
    val results: CopyResultCollection = new CopyResultCollection
    val resultHolder: Holder[CopyResultCollection] = new Holder[CopyResultCollection](results)
    val longHolder: Holder[java.lang.Long] = new Holder[java.lang.Long](-1L)
    port.copyIntoItems(sourceUrl, destinationUrlCollection, fields, SharePointBaseExample.readAll(f), longHolder, resultHolder)
    SharePointBaseExample.logger.debug("Long holder: " + longHolder.value)
    import scala.collection.JavaConversions._
    for (copyResult <- resultHolder.value.getCopyResult) {
      SharePointBaseExample.logger.debug("Destination: " + copyResult.getDestinationUrl)
      SharePointBaseExample.logger.debug("Error Message: " + copyResult.getErrorMessage)
      SharePointBaseExample.logger.debug("Error Code: " + copyResult.getErrorCode)
      if (copyResult.getErrorCode ne CopyErrorCode.SUCCESS) throw new Exception("Upload failed for: " + copyResult.getDestinationUrl + " Message: " + copyResult.getErrorMessage + " Code: " + copyResult.getErrorCode)
    }
  }
}
