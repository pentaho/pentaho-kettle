/*
 * Copyright (c) 2007 Pentaho Corporation.  All rights reserved. 
 * This software was developed by Pentaho Corporation and is provided under the terms 
 * of the GNU Lesser General Public License, Version 2.1. You may not use 
 * this file except in compliance with the license. If you need a copy of the license, 
 * please go to http://www.gnu.org/licenses/lgpl-2.1.txt. The Original Code is Pentaho 
 * Data Integration.  The Initial Developer is Pentaho Corporation.
 *
 * Software distributed under the GNU Lesser Public License is distributed on an "AS IS" 
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or  implied. Please refer to 
 * the license for the specific language governing your rights and limitations.
*/
package org.pentaho.di.cluster;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.InetAddress;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.apache.commons.httpclient.Credentials;
import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.UsernamePasswordCredentials;
import org.apache.commons.httpclient.auth.AuthScope;
import org.apache.commons.httpclient.methods.ByteArrayRequestEntity;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.InputStreamRequestEntity;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.PutMethod;
import org.apache.commons.httpclient.methods.RequestEntity;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.vfs.FileObject;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.changed.ChangedFlag;
import org.pentaho.di.core.encryption.Encr;
import org.pentaho.di.core.logging.LogChannel;
import org.pentaho.di.core.logging.LogChannelInterface;
import org.pentaho.di.core.row.ValueMeta;
import org.pentaho.di.core.variables.VariableSpace;
import org.pentaho.di.core.variables.Variables;
import org.pentaho.di.core.vfs.KettleVFS;
import org.pentaho.di.core.xml.XMLHandler;
import org.pentaho.di.core.xml.XMLInterface;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.repository.ObjectId;
import org.pentaho.di.repository.ObjectRevision;
import org.pentaho.di.repository.RepositoryDirectory;
import org.pentaho.di.repository.RepositoryDirectoryInterface;
import org.pentaho.di.repository.RepositoryElementInterface;
import org.pentaho.di.repository.RepositoryObjectType;
import org.pentaho.di.shared.SharedObjectInterface;
import org.pentaho.di.www.AddExportServlet;
import org.pentaho.di.www.AllocateServerSocketServlet;
import org.pentaho.di.www.CleanupTransServlet;
import org.pentaho.di.www.GetJobStatusServlet;
import org.pentaho.di.www.GetSlavesServlet;
import org.pentaho.di.www.GetStatusServlet;
import org.pentaho.di.www.GetTransStatusServlet;
import org.pentaho.di.www.PauseTransServlet;
import org.pentaho.di.www.RemoveJobServlet;
import org.pentaho.di.www.RemoveTransServlet;
import org.pentaho.di.www.SlaveServerDetection;
import org.pentaho.di.www.SlaveServerJobStatus;
import org.pentaho.di.www.SlaveServerStatus;
import org.pentaho.di.www.SlaveServerTransStatus;
import org.pentaho.di.www.SniffStepServlet;
import org.pentaho.di.www.StartJobServlet;
import org.pentaho.di.www.StartTransServlet;
import org.pentaho.di.www.StopJobServlet;
import org.pentaho.di.www.StopTransServlet;
import org.pentaho.di.www.WebResult;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

public class SlaveServer  extends ChangedFlag 
	implements Cloneable, SharedObjectInterface, VariableSpace, RepositoryElementInterface, XMLInterface
{
	private static Class<?> PKG = SlaveServer.class; // for i18n purposes, needed by Translator2!!   $NON-NLS-1$

	public static final String STRING_SLAVESERVER = "Slave Server"; //$NON-NLS-1$
	
    public static final String XML_TAG = "slaveserver"; //$NON-NLS-1$

    public static final RepositoryObjectType REPOSITORY_ELEMENT_TYPE = RepositoryObjectType.SLAVE_SERVER;

    private LogChannelInterface log;    
    
    private String name;
    private String hostname;
    private String port;
    private String webAppName;
    private String username;
    private String password;

    private String proxyHostname;
    private String proxyPort;
    private String nonProxyHosts;
    
    private boolean master;
    
    private boolean shared;
    private ObjectId id;
    
    private VariableSpace variables = new Variables();

	private ObjectRevision objectRevision;
	
	private Date changedDate;
    
    public SlaveServer()
    {
    	initializeVariablesFrom(null);
        id=null;
        this.log = new LogChannel(STRING_SLAVESERVER);
        this.changedDate = new Date();
    }
    
    public SlaveServer(String name, String hostname, String port, String username, String password)
    {
        this(name, hostname, port, username, password, null, null, null, false);
    }
    
    public SlaveServer(String name, String hostname, String port, String username, String password, String proxyHostname, String proxyPort, String nonProxyHosts, boolean master)
    {
        this();
        this.name     = name;
        this.hostname = hostname;
        this.port     = port;
        this.username = username;
        this.password = password;

        this.proxyHostname = proxyHostname;
        this.proxyPort = proxyPort;
        this.nonProxyHosts = nonProxyHosts;
        
        this.master = master;
        initializeVariablesFrom(null);
        this.log = new LogChannel(this);
    }
    
    public SlaveServer(Node slaveNode)
    {
        this();
        this.name       = XMLHandler.getTagValue(slaveNode, "name"); //$NON-NLS-1$
        this.hostname   = XMLHandler.getTagValue(slaveNode, "hostname"); //$NON-NLS-1$
        this.port       = XMLHandler.getTagValue(slaveNode, "port"); //$NON-NLS-1$
        this.webAppName = XMLHandler.getTagValue(slaveNode, "webAppName"); //$NON-NLS-1$
        this.username   = XMLHandler.getTagValue(slaveNode, "username"); //$NON-NLS-1$
        this.password   = Encr.decryptPasswordOptionallyEncrypted( XMLHandler.getTagValue(slaveNode, "password") ); //$NON-NLS-1$
        this.proxyHostname = XMLHandler.getTagValue(slaveNode, "proxy_hostname"); //$NON-NLS-1$
        this.proxyPort     = XMLHandler.getTagValue(slaveNode, "proxy_port"); //$NON-NLS-1$
        this.nonProxyHosts = XMLHandler.getTagValue(slaveNode, "non_proxy_hosts"); //$NON-NLS-1$
        this.master = "Y".equalsIgnoreCase( XMLHandler.getTagValue(slaveNode, "master") ); //$NON-NLS-1$ //$NON-NLS-2$
        initializeVariablesFrom(null);
        this.log = new LogChannel(this);

    }

    public String getXML()
    {
        StringBuffer xml = new StringBuffer();
        
        xml.append("<").append(XML_TAG).append(">"); //$NON-NLS-1$  //$NON-NLS-2$
        
        xml.append(XMLHandler.addTagValue("name", name, false)); //$NON-NLS-1$
        xml.append(XMLHandler.addTagValue("hostname", hostname, false)); //$NON-NLS-1$
        xml.append(XMLHandler.addTagValue("port",     port, false)); //$NON-NLS-1$
        xml.append(XMLHandler.addTagValue("webAppName", webAppName, false)); //$NON-NLS-1$
        xml.append(XMLHandler.addTagValue("username", username, false)); //$NON-NLS-1$
        xml.append(XMLHandler.addTagValue("password", Encr.encryptPasswordIfNotUsingVariables(password), false)); //$NON-NLS-1$
        xml.append(XMLHandler.addTagValue("proxy_hostname", proxyHostname, false)); //$NON-NLS-1$
        xml.append(XMLHandler.addTagValue("proxy_port", proxyPort, false)); //$NON-NLS-1$
        xml.append(XMLHandler.addTagValue("non_proxy_hosts", nonProxyHosts, false)); //$NON-NLS-1$
        xml.append(XMLHandler.addTagValue("master", master, false)); //$NON-NLS-1$

        xml.append("</").append(XML_TAG).append(">"); //$NON-NLS-1$  //$NON-NLS-2$
        
        return xml.toString();
    }
    
    public Object clone()
    {
        SlaveServer slaveServer = new SlaveServer();
        slaveServer.replaceMeta(this);
        return slaveServer;
    }

    public void replaceMeta(SlaveServer slaveServer)
    {
        this.name = slaveServer.name;
        this.hostname = slaveServer.hostname;
        this.port = slaveServer.port;
        this.webAppName = slaveServer.webAppName;
        this.username = slaveServer.username;
        this.password = slaveServer.password;
        this.proxyHostname = slaveServer.proxyHostname;
        this.proxyPort = slaveServer.proxyPort;
        this.nonProxyHosts = slaveServer.nonProxyHosts;
        this.master = slaveServer.master;
        
        this.id = slaveServer.id;
        this.shared = slaveServer.shared;
        this.setChanged(true);
    }
    
    public String toString()
    {
        return name;
    }
    
    
    public String getServerAndPort()
    {
        String realHostname = environmentSubstitute(hostname);
        if (!Const.isEmpty(realHostname)) return realHostname+getPortSpecification();
        return "Slave Server"; //$NON-NLS-1$
    }
    
    public boolean equals(Object obj)
    {
        if (!(obj instanceof SlaveServer)) {
          return false;
        }
        SlaveServer slave = (SlaveServer) obj;
        return name.equalsIgnoreCase(slave.getName());
    }
    
    public int hashCode()
    {
        return name.hashCode();
    }
    
    public String getHostname()
    {
        return hostname;
    }
    
    public void setHostname(String urlString)
    {
        this.hostname = urlString;
    }
    
    /**
     * @return the password
     */
    public String getPassword()
    {
        return password;
    }

    /**
     * @param password the password to set
     */
    public void setPassword(String password)
    {
        this.password = password;
    }

    /**
     * @return the username
     */
    public String getUsername()
    {
        return username;
    }

    /**
     * @param username the username to set
     */
    public void setUsername(String username)
    {
        this.username = username;
    }
    
    /**
     * @return the username
     */
    public String getWebAppName()
    {
        return webAppName;
    }

    /**
     * @param username the username to set
     */
    public void setWebAppName(String webAppName)
    {
        this.webAppName = webAppName;
    }
    
    /**
     * @return the nonProxyHosts
     */
    public String getNonProxyHosts()
    {
        return nonProxyHosts;
    }

    /**
     * @param nonProxyHosts the nonProxyHosts to set
     */
    public void setNonProxyHosts(String nonProxyHosts)
    {
        this.nonProxyHosts = nonProxyHosts;
    }

    /**
     * @return the proxyHostname
     */
    public String getProxyHostname()
    {
        return proxyHostname;
    }

    /**
     * @param proxyHostname the proxyHostname to set
     */
    public void setProxyHostname(String proxyHostname)
    {
        this.proxyHostname = proxyHostname;
    }

    /**
     * @return the proxyPort
     */
    public String getProxyPort()
    {
        return proxyPort;
    }

    /**
     * @param proxyPort the proxyPort to set
     */
    public void setProxyPort(String proxyPort)
    {
        this.proxyPort = proxyPort;
    }
    
    public String getPortSpecification()
    {
        String realPort = environmentSubstitute(port);
        String portSpec = ":"+realPort; //$NON-NLS-1$
        if (Const.isEmpty(realPort) || port.equals("80")) //$NON-NLS-1$
        {
            portSpec=""; //$NON-NLS-1$
        }
        return portSpec;
    }
    
    public String constructUrl(String serviceAndArguments) throws UnsupportedEncodingException
    {
        String realHostname = environmentSubstitute(hostname);
        if (!StringUtils.isEmpty(webAppName)) {
          serviceAndArguments = "/" + getWebAppName() + serviceAndArguments;
        }
        String retval =  "http://"+realHostname+getPortSpecification()+serviceAndArguments; //$NON-NLS-1$ $NON-NLS-2$
        retval = Const.replace(retval, " ", "%20"); //$NON-NLS-1$  //$NON-NLS-2$
        return retval;
    }
    
    /**
     * @return the port
     */
    public String getPort()
    {
        return port;
    }

    /**
     * @param port the port to set
     */
    public void setPort(String port)
    {
        this.port = port;
    }
    
    public PostMethod getSendByteArrayMethod(byte[] content, String service) throws Exception {
        // Prepare HTTP put
        // 
        String urlString = constructUrl(service);
        log.logDebug(BaseMessages.getString(PKG, "SlaveServer.DEBUG_ConnectingTo", urlString)); //$NON-NLS-1$
        PostMethod postMethod = new PostMethod(urlString);
        
        // Request content will be retrieved directly from the input stream
        // 
        RequestEntity entity = new ByteArrayRequestEntity(content);
        
        postMethod.setRequestEntity(entity);
        postMethod.setDoAuthentication(true);
        postMethod.addRequestHeader(new Header("Content-Type", "text/xml;charset=" + Const.XML_ENCODING));
        
        return postMethod;
    }

    public String sendXML(String xml, String service) throws Exception
    {
    	byte[] content = xml.getBytes(Const.XML_ENCODING);
    	PostMethod post = getSendByteArrayMethod(content, service);
    	
        // Get HTTP client
        // 
        HttpClient client = new HttpClient();
        addCredentials(client);
        
        // Execute request
        // 
        try
        {
            int result = client.executeMethod(post);
            
            // The status code
            log.logDebug(BaseMessages.getString(PKG, "SlaveServer.DEBUG_ResponseStatus", Integer.toString(result))); //$NON-NLS-1$
            
            // the response
            InputStream inputStream = new BufferedInputStream(post.getResponseBodyAsStream(), 1000);
            
            StringBuffer bodyBuffer = new StringBuffer();
            int c;
            while ( (c=inputStream.read())!=-1) bodyBuffer.append((char)c);
            inputStream.close();
            String bodyTmp = bodyBuffer.toString();
            
            switch(result)
            {
            case 401: // Security problem: authentication required
              // Non-internationalized message
                String message = "Authentication failed"+Const.DOSCR+Const.DOSCR+bodyTmp; //$NON-NLS-1$
                WebResult webResult = new WebResult(WebResult.STRING_ERROR, message);
                bodyBuffer.setLength(0);
                bodyBuffer.append(webResult.getXML());
                break;
            }

            String body = bodyBuffer.toString();
            

            // String body = post.getResponseBodyAsString(); 
            log.logDebug(BaseMessages.getString(PKG, "SlaveServer.DEBUG_ResponseBody",body)); //$NON-NLS-1$
            
            return body;
        } catch (Exception e) {
        	log.logError(toString(), String.format("Exception sending message to service %s", service), e);
        	throw e;
        }
        finally
        {
            // Release current connection to the connection pool once you are done
            post.releaseConnection();
            log.logDetailed(BaseMessages.getString(PKG, "SlaveServer.DETAILED_SentXmlToService", service, environmentSubstitute(hostname))); //$NON-NLS-1$
        }
    }

    /**
     * Send an exported archive over to this slave server
     * @param filename The archive to send
     * @param type The type of file to add to the slave server (AddExportServlet.TYPE_*)
     * @param load The filename to load in the archive (the .kjb or .ktr)
     * @return the XML of the web result
     * @throws Exception in case something goes awry
     */
    public String sendExport(String filename, String type, String load) throws Exception
    {
    	String serviceUrl=AddExportServlet.CONTEXT_PATH;
    	if (type!=null && load!=null) {
    		serviceUrl = serviceUrl+= "/?"+AddExportServlet.PARAMETER_TYPE+"="+type+"&"+AddExportServlet.PARAMETER_LOAD+"="+URLEncoder.encode(load, "UTF-8");
    	}

        String urlString = constructUrl(serviceUrl);
        log.logDebug(BaseMessages.getString(PKG, "SlaveServer.DEBUG_ConnectingTo", urlString)); //$NON-NLS-1$

        PutMethod putMethod = new PutMethod(urlString);
        
        // Request content will be retrieved directly from the input stream
        // 
        FileObject fileObject = KettleVFS.getFileObject(filename);
        InputStream fis = KettleVFS.getInputStream(fileObject);
        try {
          RequestEntity entity = new InputStreamRequestEntity(fis);
          
          putMethod.setRequestEntity(entity);
          putMethod.setDoAuthentication(true);
          putMethod.addRequestHeader(new Header("Content-Type", "binary/zip"));
      	
          // Get HTTP client
          // 
          HttpClient client = new HttpClient();
          addCredentials(client);
          
          // Execute request
          // 
          try
          {
              int result = client.executeMethod(putMethod);
              
              // The status code
              log.logDebug(BaseMessages.getString(PKG, "SlaveServer.DEBUG_ResponseStatus", Integer.toString(result))); //$NON-NLS-1$
              
              // the response
              InputStream inputStream = new BufferedInputStream(putMethod.getResponseBodyAsStream(), 1000);
              
              StringBuffer bodyBuffer = new StringBuffer();
              int c;
              while ( (c=inputStream.read())!=-1) bodyBuffer.append((char)c);
              inputStream.close();
              String bodyTmp = bodyBuffer.toString();
              
              switch(result)
              {
              case 401: // Security problem: authentication required
                // Non-internationalized message
                  String message = "Authentication failed"+Const.DOSCR+Const.DOSCR+bodyTmp; //$NON-NLS-1$
                  WebResult webResult = new WebResult(WebResult.STRING_ERROR, message);
                  bodyBuffer.setLength(0);
                  bodyBuffer.append(webResult.getXML());
                  break;
              }
  
              String body = bodyBuffer.toString();
              
  
              // String body = post.getResponseBodyAsString(); 
              log.logDebug(BaseMessages.getString(PKG, "SlaveServer.DEBUG_ResponseBody",body)); //$NON-NLS-1$
              
              return body;
          }
          finally
          {
              // Release current connection to the connection pool once you are done
              putMethod.releaseConnection();
              log.logDetailed(BaseMessages.getString(PKG, "SlaveServer.DETAILED_SentExportToService", AddExportServlet.CONTEXT_PATH, environmentSubstitute(hostname))); //$NON-NLS-1$
          }
        } finally {
          try {
            fis.close();
          } catch (IOException ignored) {
            // nothing to do here...
          }
        }
    }

    public void addCredentials(HttpClient client)
    {
      if (StringUtils.isEmpty(webAppName)) {
        client.getState().setCredentials
              (
                new AuthScope(environmentSubstitute(hostname), Const.toInt(environmentSubstitute(port), 80), "Kettle"), //$NON-NLS-1$
                new UsernamePasswordCredentials(environmentSubstitute(username), environmentSubstitute(password))
              );
      } else {        
        Credentials creds = new UsernamePasswordCredentials(environmentSubstitute(username), environmentSubstitute(password));
        client.getState().setCredentials(AuthScope.ANY, creds);
        client.getParams().setAuthenticationPreemptive(true);      
      }
    }

    /**
     * @return the master
     */
    public boolean isMaster()
    {
        return master;
    }

    /**
     * @param master the master to set
     */
    public void setMaster(boolean master)
    {
        this.master = master;
    }

    public String execService(String service) throws Exception
    {
        // Prepare HTTP get
        // 
        HttpClient client = new HttpClient();
        addCredentials(client);
        HttpMethod method = new GetMethod(constructUrl(service));
        
        // Execute request
        // 
        try
        {
            int result = client.executeMethod(method);
            
            // The status code
            log.logDebug(BaseMessages.getString(PKG, "SlaveServer.DEBUG_ResponseStatus", Integer.toString(result))); //$NON-NLS-1$
            
            String body = method.getResponseBodyAsString();
            
            log.logDetailed(BaseMessages.getString(PKG, "SlaveServer.DETAILED_FinishedReading", Integer.toString(body.getBytes().length))); //$NON-NLS-1$
            log.logDebug(BaseMessages.getString(PKG, "SlaveServer.DEBUG_ResponseBody",body)); //$NON-NLS-1$
            
            return body;
        }
        finally
        {
            // Release current connection to the connection pool once you are done
            method.releaseConnection();
            log.logDetailed(BaseMessages.getString(PKG, "SlaveServer.DETAILED_ExecutedService", service, hostname) ); //$NON-NLS-1$
        }

    }

    public SlaveServerStatus getStatus() throws Exception
    {
        String xml = execService(GetStatusServlet.CONTEXT_PATH+"/?xml=Y"); //$NON-NLS-1$
        return SlaveServerStatus.fromXML(xml);
    }
    
    public List<SlaveServerDetection> getSlaveServerDetections() throws Exception
    {
        String xml = execService(GetSlavesServlet.CONTEXT_PATH+"/"); //$NON-NLS-1$
        Document document = XMLHandler.loadXMLString(xml);
        Node detectionsNode = XMLHandler.getSubNode(document, GetSlavesServlet.XML_TAG_SLAVESERVER_DETECTIONS);
        int nrDetections = XMLHandler.countNodes(detectionsNode, SlaveServerDetection.XML_TAG);
        
        List<SlaveServerDetection> detections = new ArrayList<SlaveServerDetection>();
        for (int i=0;i<nrDetections;i++) {
        	Node detectionNode = XMLHandler.getSubNodeByNr(detectionsNode, SlaveServerDetection.XML_TAG, i);
        	SlaveServerDetection detection = new SlaveServerDetection(detectionNode);
        	detections.add(detection);
        }
        return detections;
    }

    public SlaveServerTransStatus getTransStatus(String transName, String carteObjectId, int startLogLineNr) throws Exception
    {
        String xml = execService(GetTransStatusServlet.CONTEXT_PATH+"/?name="+URLEncoder.encode(transName, "UTF-8")+"&id="+Const.NVL(carteObjectId, "")+"&xml=Y&from="+startLogLineNr); //$NON-NLS-1$  //$NON-NLS-2$
        return SlaveServerTransStatus.fromXML(xml);
    }
    
    public SlaveServerJobStatus getJobStatus(String jobName, String carteObjectId, int startLogLineNr) throws Exception
    {
        String xml = execService(GetJobStatusServlet.CONTEXT_PATH+"/?name="+URLEncoder.encode(jobName, "UTF-8")+"&id="+Const.NVL(carteObjectId, "")+"&xml=Y&from="+startLogLineNr); //$NON-NLS-1$  //$NON-NLS-2$
        return SlaveServerJobStatus.fromXML(xml);
    }
    
    public WebResult stopTransformation(String transName, String carteObjectId) throws Exception
    {
        String xml = execService(StopTransServlet.CONTEXT_PATH+"/?name="+URLEncoder.encode(transName, "UTF-8")+"&id="+Const.NVL(carteObjectId, "")+"&xml=Y"); //$NON-NLS-1$  //$NON-NLS-2$
        return WebResult.fromXMLString(xml);
    }

    public WebResult pauseResumeTransformation(String transName, String carteObjectId) throws Exception
    {
        String xml = execService(PauseTransServlet.CONTEXT_PATH+"/?name="+URLEncoder.encode(transName, "UTF-8")+"&id="+Const.NVL(carteObjectId, "")+"&xml=Y"); //$NON-NLS-1$  //$NON-NLS-2$
        return WebResult.fromXMLString(xml);
    }

    public WebResult removeTransformation(String transName, String carteObjectId) throws Exception
    {
        String xml = execService(RemoveTransServlet.CONTEXT_PATH+"/?name="+URLEncoder.encode(transName, "UTF-8")+"&id="+Const.NVL(carteObjectId, "")+"&xml=Y"); //$NON-NLS-1$  //$NON-NLS-2$
        return WebResult.fromXMLString(xml);
    }

    public WebResult removeJob(String jobName, String carteObjectId) throws Exception
    {
        String xml = execService(RemoveJobServlet.CONTEXT_PATH+"/?name="+URLEncoder.encode(jobName, "UTF-8")+"&id="+Const.NVL(carteObjectId, "")+"&xml=Y"); //$NON-NLS-1$  //$NON-NLS-2$
        return WebResult.fromXMLString(xml);
    }

    public WebResult stopJob(String transName, String carteObjectId) throws Exception
    {
        String xml = execService(StopJobServlet.CONTEXT_PATH+"/?name="+URLEncoder.encode(transName, "UTF-8")+"&xml=Y&id="+Const.NVL(carteObjectId, "")); //$NON-NLS-1$  //$NON-NLS-2$
        return WebResult.fromXMLString(xml);
    }
    
    public WebResult startTransformation(String transName, String carteObjectId) throws Exception
    {
        String xml = execService(StartTransServlet.CONTEXT_PATH+"/?name="+URLEncoder.encode(transName, "UTF-8")+"&id="+Const.NVL(carteObjectId, "")+"&xml=Y");  //$NON-NLS-1$ //$NON-NLS-2$
        return WebResult.fromXMLString(xml);
    }
    
    public WebResult startJob(String jobName, String carteObjectId) throws Exception
    {
        String xml = execService(StartJobServlet.CONTEXT_PATH+"/?name="+URLEncoder.encode(jobName, "UTF-8")+"&xml=Y&id="+Const.NVL(carteObjectId, ""));  //$NON-NLS-1$ //$NON-NLS-2$
        return WebResult.fromXMLString(xml);
    }

    public WebResult cleanupTransformation(String transName, String carteObjectId) throws Exception
    {
        String xml = execService(CleanupTransServlet.CONTEXT_PATH+"/?name="+URLEncoder.encode(transName, "UTF-8")+"&id="+Const.NVL(carteObjectId, "")+"&xml=Y"); //$NON-NLS-1$  //$NON-NLS-2$
        return WebResult.fromXMLString(xml);
    }

    public WebResult deAllocateServerSockets(String transName, String carteObjectId) throws Exception
    {
        String xml = execService(CleanupTransServlet.CONTEXT_PATH+"/?name="+URLEncoder.encode(transName, "UTF-8")+"&id="+Const.NVL(carteObjectId, "")+"&xml=Y&sockets=Y"); //$NON-NLS-1$  //$NON-NLS-2$
        return WebResult.fromXMLString(xml);
    }

    public static SlaveServer findSlaveServer(List<SlaveServer> slaveServers, String name)
    {
        for (SlaveServer slaveServer : slaveServers)
        {
            if (slaveServer.getName()!=null && slaveServer.getName().equalsIgnoreCase(name)) return slaveServer; 
        }
        return null;
    }

    public static SlaveServer findSlaveServer(List<SlaveServer> slaveServers, ObjectId id)
    {
        for (SlaveServer slaveServer : slaveServers)
        {
            if (slaveServer.getObjectId()!=null && slaveServer.getObjectId().equals(id)) return slaveServer;
        }
        return null;
    }

    public static String[] getSlaveServerNames(List<SlaveServer> slaveServers)
    {
        String[] names = new String[slaveServers.size()];
        for (int i=0;i<slaveServers.size();i++)
        {
            SlaveServer slaveServer = slaveServers.get(i);
            names[i] = slaveServer.getName();
        }
        return names;
    }
    
    public int allocateServerSocket(int portRangeStart, String hostname, String transformationName, String sourceSlaveName, String sourceStepName, String sourceStepCopy, String targetSlaveName, String targetStepName, String targetStepCopy) throws Exception {

    	// Look up the IP address of the given hostname
    	// Only this way we'll be to allocate on the correct host.
    	//
    	InetAddress inetAddress = InetAddress.getByName(hostname);
    	String address = inetAddress.getHostAddress();
    	
    	String service=AllocateServerSocketServlet.CONTEXT_PATH+"/?";
    	service += AllocateServerSocketServlet.PARAM_RANGE_START+"="+Integer.toString(portRangeStart);
    	service += "&" + AllocateServerSocketServlet.PARAM_HOSTNAME+"="+address;
    	service += "&" + AllocateServerSocketServlet.PARAM_TRANSFORMATION_NAME+"="+URLEncoder.encode(transformationName, "UTF-8");
    	service += "&" + AllocateServerSocketServlet.PARAM_SOURCE_SLAVE+"="+URLEncoder.encode(sourceSlaveName, "UTF-8");
    	service += "&" + AllocateServerSocketServlet.PARAM_SOURCE_STEPNAME+"="+URLEncoder.encode(sourceStepName, "UTF-8");
    	service += "&" + AllocateServerSocketServlet.PARAM_SOURCE_STEPCOPY+"="+URLEncoder.encode(sourceStepCopy, "UTF-8");
    	service += "&" + AllocateServerSocketServlet.PARAM_TARGET_SLAVE+"="+URLEncoder.encode(targetSlaveName, "UTF-8");
    	service += "&" + AllocateServerSocketServlet.PARAM_TARGET_STEPNAME+"="+URLEncoder.encode(targetStepName, "UTF-8");
    	service += "&" + AllocateServerSocketServlet.PARAM_TARGET_STEPCOPY+"="+URLEncoder.encode(targetStepCopy, "UTF-8");
    	service += "&xml=Y";
    	String xml = execService(service);
    	Document doc = XMLHandler.loadXMLString(xml);
    	String portString = XMLHandler.getTagValue(doc, AllocateServerSocketServlet.XML_TAG_PORT);
    	
    	int port = Const.toInt(portString, -1);
    	if (port<0) {
    		throw new Exception("Unable to retrieve port from service : "+service+", received : \n"+xml);
    	}

    	return port;
    }

    public String getName()
    {
        return name;
    }
    
    public void setName(String name)
    {
        this.name = name;
    }

    public boolean isShared()
    {
        return shared;
    }

    public void setShared(boolean shared)
    {
        this.shared = shared;
    }

	public void copyVariablesFrom(VariableSpace space) 
	{
		variables.copyVariablesFrom(space);		
	}

	public String environmentSubstitute(String aString) 
	{
		return variables.environmentSubstitute(aString);
	}

	public String[] environmentSubstitute(String aString[]) 
	{
		return variables.environmentSubstitute(aString);
	}		

	public VariableSpace getParentVariableSpace() 
	{
		return variables.getParentVariableSpace();
	}

	public void setParentVariableSpace(VariableSpace parent) 
	{
		variables.setParentVariableSpace(parent);
	}

	public String getVariable(String variableName, String defaultValue) 
	{
		return variables.getVariable(variableName, defaultValue);
	}

	public String getVariable(String variableName) 
	{
		return variables.getVariable(variableName);
	}
	
	public boolean getBooleanValueOfVariable(String variableName, boolean defaultValue) {
		if (!Const.isEmpty(variableName))
		{
			String value = environmentSubstitute(variableName);
			if (!Const.isEmpty(value))
			{
				return ValueMeta.convertStringToBoolean(value);
			}
		}
		return defaultValue;
	}

	public void initializeVariablesFrom(VariableSpace parent) 
	{
		variables.initializeVariablesFrom(parent);	
	}

	public String[] listVariables() 
	{
		return variables.listVariables();
	}

	public void setVariable(String variableName, String variableValue) 
	{
		variables.setVariable(variableName, variableValue);		
	}

	public void shareVariablesWith(VariableSpace space) 
	{
		variables = space;		
	}

	public void injectVariables(Map<String,String> prop) 
	{
		variables.injectVariables(prop);		
	}

	public ObjectId getObjectId() {
		return id;
	}
	
	public void setObjectId(ObjectId id) {
		this.id = id;
	}

	/**
	 * Not used in this case, simply return root /
	 */
	public RepositoryDirectoryInterface getRepositoryDirectory() {
		return new RepositoryDirectory();
	}
	
	public void setRepositoryDirectory(RepositoryDirectoryInterface repositoryDirectory) {
		throw new RuntimeException("Setting a directory on a database connection is not supported");
	}
	
	public RepositoryObjectType getRepositoryElementType() {
		return REPOSITORY_ELEMENT_TYPE;
	}
	
	public ObjectRevision getObjectRevision() {
		return objectRevision;
	}

	public void setObjectRevision(ObjectRevision objectRevision) {
		this.objectRevision = objectRevision;
	}
	public String getDescription() {
		// NOT USED
		return null;
	}
	
	public void setDescription(String description) {
		// NOT USED
	}

	/**
	 * Sniff rows on a the slave server, return xml containing the row metadata and data.
	 * 
	 * @param transName
	 * @param stepName
	 * @param copyNr
	 * @param lines
	 * @return
	 * @throws Exception
	 */
	public String sniffStep(String transName, String stepName, String copyNr, int lines, String type) throws Exception {
		String xml = execService(
				SniffStepServlet.CONTEXT_PATH+"/?trans="+URLEncoder.encode(transName, "UTF-8")+
				"&step="+URLEncoder.encode(stepName, "UTF-8")+
				"&copynr="+copyNr+
				"&type="+type+
				"&lines="+lines+
				"&xml=Y"); //$NON-NLS-1$  //$NON-NLS-2$
		return xml;
	}

	/**
	 * @return the changedDate
	 */
	public Date getChangedDate() {
		return changedDate;
	}

	/**
	 * @param changedDate the changedDate to set
	 */
	public void setChangedDate(Date changedDate) {
		this.changedDate = changedDate;
	}
}