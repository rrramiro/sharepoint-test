package com.jw.sharepoint.examples

import java.io.File
import java.nio.file.{Files, Paths}
import java.util.Properties

import org.scalatest.FunSuite
import com.microsoft.sharepoint.ws.{CopySoap12Bindings, DestinationUrlCollection, FieldInformation, FieldInformationCollection}

import scala.concurrent.Await
import scala.concurrent.duration._
import scalaxb.{Base64Binary, SoapClientsAsync}

class SharePointUploadTest extends FunSuite{
  test("upload"){
    val properties = new Properties{
      load(getClass.getClassLoader.getResourceAsStream("SharePointUploadDocumentExample.properties"))
    }
    val remote = new CopySoap12Bindings with SoapClientsAsync with DispatchHttpAuthClientsAsync {
      val user = properties.getProperty("username")
      val password = properties.getProperty("password")
      val hostname = "infopoint"
    }

    val source = getClass.getClassLoader.getResource(properties.getProperty("copy.sourceFile"))
    val sourceUrl = Some(source.toString)
    val destinationUrls = Some(DestinationUrlCollection(Seq(
      Some(properties.getProperty("copy.location"))
    )))

    val fieldInformation = scalaxb.fromXML[FieldInformation](<FieldInformation Type="Text" Id="" DisplayName="Title" Value={new File(source.getFile).getName}/>)
    val fields = Some(FieldInformationCollection(Seq(Some(fieldInformation))))
    val data = Files.readAllBytes(Paths.get(source.toURI))
    val stream = Some(Base64Binary("test"))

    val f = remote.service.copyIntoItems(sourceUrl, destinationUrls, fields, stream)
    val result = Await.result(f, 2 minutes)
    result.Results.foreach{_.CopyResult.foreach{_.foreach{_.ErrorMessage.foreach(println)}}}
  }
}
