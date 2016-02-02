package com.jw.sharepoint.examples

import java.net.Authenticator
import java.net.PasswordAuthentication
import java.util.Properties

class SharepointAuthenticator(properties: Properties) extends Authenticator {
  override def getPasswordAuthentication: PasswordAuthentication = {
    return new PasswordAuthentication(properties.getProperty("username"), properties.getProperty("password").toCharArray)
  }
}
