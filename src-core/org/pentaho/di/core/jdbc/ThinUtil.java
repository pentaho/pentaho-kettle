package org.pentaho.di.core.jdbc;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.httpclient.Credentials;
import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.UsernamePasswordCredentials;
import org.apache.commons.httpclient.auth.AuthScope;
import org.apache.commons.httpclient.methods.GetMethod;
import org.pentaho.di.core.exception.KettleException;

public class ThinUtil {
  public static String getStringFromService(String url, ThinConnection connection) throws KettleException {
    try {
      HttpClient client = null;
      DataInputStream dataInputStream = null;
      
      try {
        client = new HttpClient();
        
        Credentials creds = new UsernamePasswordCredentials(connection.getUsername(), connection.getPassword());
        client.getState().setCredentials(AuthScope.ANY, creds);
        
        GetMethod method = new GetMethod(url+"/");

        method.setDoAuthentication(true);
        method.addRequestHeader(new Header("Content-Type", "text/xml"));

        int result = client.executeMethod(method);
        
        if (result==500) {
          String response = getStreamString(method.getResponseBodyAsStream());
          throw new KettleException("Error 500 reading data from slave server: "+response);
        } 
        if (result==401) {
          String response = getStreamString(method.getResponseBodyAsStream());
          throw new KettleException("Access denied error 401 received while attempting to read data from server: "+response);
        }
        if (result!=200) {
          String response = getStreamString(method.getResponseBodyAsStream());
          throw new KettleException("Error received while attempting to read data from server: "+response);
        }

        return getStreamString(method.getResponseBodyAsStream());
      } finally {
        if (dataInputStream!=null) {
          dataInputStream.close();
        }
      }
    } catch(Exception e) {
      throw new KettleException("Unable to get open query for URL: "+url, e);
    }
  }
  
  private static String getStreamString(InputStream inputStream) throws IOException {
    StringBuffer bodyBuffer = new StringBuffer();
    int c;
    while ( (c=inputStream.read())!=-1) bodyBuffer.append((char)c);
    return bodyBuffer.toString();
    
  }
}
