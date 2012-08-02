package org.pentaho.di.cluster;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.zip.GZIPInputStream;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.httpclient.Credentials;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.UsernamePasswordCredentials;
import org.apache.commons.httpclient.auth.AuthScope;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.lang.StringUtils;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.encryption.Encr;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.variables.VariableSpace;

public class HttpUtil {
  
  public static String execService(VariableSpace space, String hostname, String port, String webAppName, String serviceAndArguments, String username, String password, String proxyHostname, String proxyPort, String nonProxyHosts) throws Exception
  {
      // Prepare HTTP get
      // 
      HttpClient client = SlaveConnectionManager.getInstance().createHttpClient();
      addCredentials(client, space, hostname, port, webAppName, username, password);
      addProxy(client, space, hostname, proxyHostname, proxyPort, nonProxyHosts);
      String urlString = constructUrl(space, hostname, port, webAppName, serviceAndArguments);
      HttpMethod method = new GetMethod(urlString);
      
      // Execute request
      // 
      InputStream inputStream=null;
      BufferedInputStream bufferedInputStream=null;

      try
      {
          int result = client.executeMethod(method);
          if (result!=200) {
            throw new KettleException("Response code "+result+" received while querying "+urlString);
          }
          
          // the response
          //
          inputStream = method.getResponseBodyAsStream();
          bufferedInputStream = new BufferedInputStream(inputStream, 1000);
          
          StringBuffer bodyBuffer = new StringBuffer();
          int c;
          while ( (c=bufferedInputStream.read())!=-1) bodyBuffer.append((char)c);

          String body = bodyBuffer.toString();
          
          return body;
      }
      finally
      {
        if (bufferedInputStream!=null) {
          bufferedInputStream.close();
        }
        if (inputStream!=null) {
          inputStream.close();
        }

          // Release current connection to the connection pool once you are done
          method.releaseConnection();            
      }

  }
  
  public static String constructUrl(VariableSpace space, String hostname, String port, String webAppName, String serviceAndArguments) throws UnsupportedEncodingException
  {
      String realHostname = space.environmentSubstitute(hostname);
      if (!StringUtils.isEmpty(webAppName)) {
        serviceAndArguments = "/" + space.environmentSubstitute(webAppName) + serviceAndArguments;
      }
      String retval =  "http://"+realHostname+getPortSpecification(space, port)+serviceAndArguments; //$NON-NLS-1$ $NON-NLS-2$
      retval = Const.replace(retval, " ", "%20"); //$NON-NLS-1$  //$NON-NLS-2$
      return retval;
  }
  
  public static String getPortSpecification(VariableSpace space, String port)
  {
      String realPort = space.environmentSubstitute(port);
      String portSpec = ":"+realPort; //$NON-NLS-1$
      if (Const.isEmpty(realPort) || port.equals("80")) //$NON-NLS-1$
      {
          portSpec=""; //$NON-NLS-1$
      }
      return portSpec;
  }

  public static void addProxy(HttpClient client, VariableSpace space, String hostname, String proxyHostname, String proxyPort, String nonProxyHosts)
  {
      String host = space.environmentSubstitute(hostname);
      String phost = space.environmentSubstitute(proxyHostname);
      String pport = space.environmentSubstitute(proxyPort);
      String nonprox = space.environmentSubstitute(nonProxyHosts);
      
      
      /** added by shingo.yamagami@ksk-sol.jp **/
      if (!Const.isEmpty(phost) && !Const.isEmpty(pport)) 
      {
          // skip applying proxy if non-proxy host matches
          if (!Const.isEmpty(nonprox) && !Const.isEmpty(host) && host.matches(nonprox))
          {
              return;
          }
          client.getHostConfiguration().setProxy(phost, Integer.parseInt(pport));
      }
      /** added by shingo.yamagami@ksk-sol.jp **/  
  }


  public static void addCredentials(HttpClient client, VariableSpace space, String hostname, String port, String webAppName, String username, String password)
  {
    if (StringUtils.isEmpty(webAppName)) {
      client.getState().setCredentials
            (
              new AuthScope(space.environmentSubstitute(hostname), Const.toInt(space.environmentSubstitute(port), 80), "Kettle"), //$NON-NLS-1$
              new UsernamePasswordCredentials(space.environmentSubstitute(username), Encr.decryptPasswordOptionallyEncrypted(space.environmentSubstitute(password)))
            );
    } else {        
      Credentials creds = new UsernamePasswordCredentials(space.environmentSubstitute(username), Encr.decryptPasswordOptionallyEncrypted(space.environmentSubstitute(password)));
      client.getState().setCredentials(AuthScope.ANY, creds);
      client.getParams().setAuthenticationPreemptive(true);      
    }
  }

  public static String decodeBase64ZippedString(String loggingString64) throws IOException {
    byte[] bytes = new byte[] {};
    if (loggingString64!=null) bytes = Base64.decodeBase64(loggingString64.getBytes());
    if (bytes.length>0)
    {
        ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
        GZIPInputStream gzip = new GZIPInputStream(bais);
        int c;
        StringBuffer buffer = new StringBuffer();
        while ( (c=gzip.read())!=-1) buffer.append((char)c);
        gzip.close();
        
        return buffer.toString();
    }
    else
    {
        return "";
    }

  }
  
}
