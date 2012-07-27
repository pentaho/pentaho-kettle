package org.pentaho.di.cluster;

import java.io.BufferedInputStream;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;

import org.apache.commons.httpclient.Credentials;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.UsernamePasswordCredentials;
import org.apache.commons.httpclient.auth.AuthScope;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.lang.StringUtils;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.encryption.Encr;
import org.pentaho.di.core.logging.LogChannelInterface;
import org.pentaho.di.core.variables.VariableSpace;
import org.pentaho.di.i18n.BaseMessages;

public class HttpUtil {
  private static Class<?> PKG = HttpUtil.class; // for i18n purposes, needed by Translator2!!   $NON-NLS-1$


  public static String execService(LogChannelInterface log, VariableSpace space, String hostname, String port, String webAppName, String serviceAndArguments, String username, String password, String proxyHostname, String proxyPort, String nonProxyHosts) throws Exception
  {
      // Prepare HTTP get
      // 
      HttpClient client = SlaveConnectionManager.getInstance().createHttpClient();
      addCredentials(client, space, hostname, port, webAppName, username, password);
      addProxy(client, space, hostname, proxyHostname, proxyPort, nonProxyHosts);
      HttpMethod method = new GetMethod(constructUrl(space, hostname, port, webAppName, serviceAndArguments));
      
      // Execute request
      // 
      InputStream inputStream=null;
      BufferedInputStream bufferedInputStream=null;

      try
      {
          int result = client.executeMethod(method);
          
          // The status code
          log.logDebug(BaseMessages.getString(PKG, "SlaveServer.DEBUG_ResponseStatus", Integer.toString(result))); //$NON-NLS-1$

          // the response
          //
          inputStream = method.getResponseBodyAsStream();
          bufferedInputStream = new BufferedInputStream(inputStream, 1000);
          
          StringBuffer bodyBuffer = new StringBuffer();
          int c;
          while ( (c=bufferedInputStream.read())!=-1) bodyBuffer.append((char)c);

          String body = bodyBuffer.toString();
          
          log.logDetailed(BaseMessages.getString(PKG, "SlaveServer.DETAILED_FinishedReading", Integer.toString(body.getBytes().length))); //$NON-NLS-1$
          log.logDebug(BaseMessages.getString(PKG, "SlaveServer.DEBUG_ResponseBody",body)); //$NON-NLS-1$
          
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
          log.logDetailed(BaseMessages.getString(PKG, "SlaveServer.DETAILED_ExecutedService", serviceAndArguments, hostname) ); //$NON-NLS-1$
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
  
}
