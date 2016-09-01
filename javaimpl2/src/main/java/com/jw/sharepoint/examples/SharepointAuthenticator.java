package com.jw.sharepoint.examples;

import java.net.Authenticator;
import java.net.PasswordAuthentication;
import java.util.Properties;

public class SharepointAuthenticator extends Authenticator{
    
	private Properties properties;
	
	public SharepointAuthenticator(Properties props){
		properties = props;
	}
    
    public PasswordAuthentication getPasswordAuthentication () {
	
	    return new PasswordAuthentication (        	
        	properties.getProperty("username"),properties.getProperty("password").toCharArray());
	}


}
