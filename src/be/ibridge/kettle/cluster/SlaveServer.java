package be.ibridge.kettle.cluster;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.Authenticator;
import java.net.MalformedURLException;
import java.net.PasswordAuthentication;
import java.net.URL;
import java.net.URLConnection;
import java.util.List;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.UsernamePasswordCredentials;
import org.apache.commons.httpclient.auth.AuthScope;
import org.apache.commons.httpclient.methods.ByteArrayRequestEntity;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.RequestEntity;
import org.w3c.dom.Node;

import be.ibridge.kettle.core.ChangedFlag;
import be.ibridge.kettle.core.Const;
import be.ibridge.kettle.core.Encr;
import be.ibridge.kettle.core.LogWriter;
import be.ibridge.kettle.core.Row;
import be.ibridge.kettle.core.SharedObjectInterface;
import be.ibridge.kettle.core.XMLHandler;
import be.ibridge.kettle.core.exception.KettleDatabaseException;
import be.ibridge.kettle.core.util.StringUtil;
import be.ibridge.kettle.repository.Repository;
import be.ibridge.kettle.www.GetStatusHandler;
import be.ibridge.kettle.www.GetTransStatusHandler;
import be.ibridge.kettle.www.SlaveServerStatus;
import be.ibridge.kettle.www.SlaveServerTransStatus;
import be.ibridge.kettle.www.StartTransHandler;
import be.ibridge.kettle.www.StopTransHandler;
import be.ibridge.kettle.www.WebResult;

public class SlaveServer extends ChangedFlag implements Cloneable, SharedObjectInterface
{
    public static final String XML_TAG = "slaveserver";

    private static LogWriter log = LogWriter.getInstance();
    
    private String name;
    private String hostname;
    private String port;
    private String username;
    private String password;

    private String proxyHostname;
    private String proxyPort;
    private String nonProxyHosts;
    
    private boolean master;
    
    private boolean shared;
    private long id;
    
    public SlaveServer()
    {
        id=-1L;
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
    }
    
    public SlaveServer(Node slaveNode)
    {
        this();
        this.name       = XMLHandler.getTagValue(slaveNode, "name");
        this.hostname   = XMLHandler.getTagValue(slaveNode, "hostname");
        this.port       = XMLHandler.getTagValue(slaveNode, "port");
        this.username   = XMLHandler.getTagValue(slaveNode, "username");
        this.password   = Encr.decryptPasswordOptionallyEncrypted( XMLHandler.getTagValue(slaveNode, "password") );
        this.proxyHostname = XMLHandler.getTagValue(slaveNode, "proxy_hostname");
        this.proxyPort     = XMLHandler.getTagValue(slaveNode, "proxy_port");
        this.nonProxyHosts = XMLHandler.getTagValue(slaveNode, "non_proxy_hosts");
        this.master = "Y".equalsIgnoreCase( XMLHandler.getTagValue(slaveNode, "master") );
    }

    public String getXML()
    {
        StringBuffer xml = new StringBuffer();
        
        xml.append("<"+XML_TAG+">");
        
        xml.append(XMLHandler.addTagValue("name", name, false));
        xml.append(XMLHandler.addTagValue("hostname", hostname, false));
        xml.append(XMLHandler.addTagValue("port",     port, false));
        xml.append(XMLHandler.addTagValue("username", username, false));
        xml.append(XMLHandler.addTagValue("password", Encr.encryptPasswordIfNotUsingVariables(password), false));
        xml.append(XMLHandler.addTagValue("proxy_hostname", proxyHostname, false));
        xml.append(XMLHandler.addTagValue("proxy_port", proxyPort, false));
        xml.append(XMLHandler.addTagValue("non_proxy_hosts", nonProxyHosts, false));
        xml.append(XMLHandler.addTagValue("master", master, false));

        xml.append("</"+XML_TAG+">");
        
        return xml.toString();
    }

    public void saveRep(Repository rep) throws KettleDatabaseException
    {
        saveRep(rep, -1L);
    }
    
    public void saveRep(Repository rep, long id_transformation) throws KettleDatabaseException
    {
        setId(rep.getSlaveID(name));
        
        if (getId()<0)
        {
            setId(rep.insertSlave(this));
        }
        else
        {
            rep.updateSlave(this);
        }
        
        // Save the trans-slave relationship too.
        if (id_transformation>=0) rep.insertTransformationSlave(id_transformation, getId());
    }
    
    public SlaveServer(Repository rep, long id_slave_server) throws KettleDatabaseException
    {
        this();
        
        setId(id_slave_server);
        
        Row row = rep.getSlaveServer(id_slave_server);
        if (row==null)
        {
            throw new KettleDatabaseException("Internal repository error: slave server with id "+id_slave_server+" could not be found!");
        }
        
        name          = row.getString("NAME", null);
        hostname      = row.getString("HOST_NAME", null);
        port          = row.getString("PORT", null);
        username      = row.getString("USERNAME", null);
        password      = row.getString("PASSWORD", null);
        proxyHostname = row.getString("PROXY_HOST_NAME", null);
        proxyPort     = row.getString("PROXY_PORT", null);
        nonProxyHosts = row.getString("NON_PROXY_HOSTS", null);
        master        = row.getBoolean("MASTER", false);
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
        this.username = slaveServer.username;
        this.password = slaveServer.password;
        this.proxyHostname = slaveServer.proxyHostname;
        this.proxyPort = slaveServer.proxyPort;
        this.nonProxyHosts = slaveServer.nonProxyHosts;
        
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
        String realHostname = StringUtil.environmentSubstitute(hostname);
        if (!Const.isEmpty(realHostname)) return realHostname+getPortSpecification();
        return "Slave Server";
    }
    
    public boolean equals(Object obj)
    {
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
        String realPort = StringUtil.environmentSubstitute(port);
        String portSpec = ":"+realPort;
        if (Const.isEmpty(realPort) || port.equals("80"))
        {
            portSpec="";
        }
        return portSpec;
    }
    
    public String constructUrl(String serviceAndArguments)
    {
        String realHostname = StringUtil.environmentSubstitute(hostname);
        String retval =  "http://"+realHostname+getPortSpecification()+serviceAndArguments;
        retval = Const.replace(retval, " ", "%20");
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

    public String sendXML(String xml, String service) throws Exception
    {
        // The content
        // 
        byte[] content = xml.getBytes(Const.XML_ENCODING);
        
        // Prepare HTTP post
        // 
        PostMethod post = new PostMethod(constructUrl(service));
        
        // GetMethod post = new GetMethod("http://127.0.0.1/kettle/status");
        
        // Request content will be retrieved directly from the input stream
        // 
        RequestEntity entity = new ByteArrayRequestEntity(content);
        
        post.setRequestEntity(entity);
        post.setDoAuthentication(true);
        
        // post.setContentChunked(true);
        
        // Get HTTP client
        // 
        HttpClient client = new HttpClient();
        client.getState().setCredentials(
                new AuthScope(hostname, Const.toInt(port, 80), "Kettle"),
                new UsernamePasswordCredentials(username, password)
                );
        
        // Execute request
        // 
        try
        {
            int result = client.executeMethod(post);
            
            // The status code
            log.logDebug(toString(), "Response status code: " + result);
            
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
                String message = "Authentication failed"+Const.DOSCR+Const.DOSCR+bodyTmp;
                WebResult webResult = new WebResult(WebResult.STRING_ERROR, message);
                bodyBuffer.setLength(0);
                bodyBuffer.append(webResult.getXML());
                break;
            }

            String body = bodyBuffer.toString();
            

            // String body = post.getResponseBodyAsString(); 
            log.logDebug(toString(), "Response body: "+body);
            
            return body;
        }
        finally
        {
            // Release current connection to the connection pool once you are done
            post.releaseConnection();
            log.logDetailed(toString(), "Sent XML to service ["+service+"] on host ["+hostname+"]");
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
        HttpClient httpclient = new HttpClient();
        HttpMethod method = new GetMethod(constructUrl(service));
        
        // Execute request
        // 
        try
        {
            int result = httpclient.executeMethod(method);
            
            // The status code
            log.logDebug(toString(), "Response status code: " + result);
            
            // the response
            InputStream inputStream = new BufferedInputStream(method.getResponseBodyAsStream());
            
            StringBuffer bodyBuffer = new StringBuffer();
            int c;
            while ( (c=inputStream.read())!=-1) 
            {
                bodyBuffer.append((char)c);
            }
            inputStream.close();
            
            String body = bodyBuffer.toString();

            log.logDetailed(toString(), "Finished reading "+bodyBuffer.length()+" bytes from server.");
            log.logDebug(toString(), "Response body: "+body);
            
            return body;
        }
        finally
        {
            // Release current connection to the connection pool once you are done
            method.releaseConnection();
            log.logDetailed(toString(), "Executed service ["+service+"] on host ["+hostname+"]");
        }

    }

    /**
     * Contact the server and get back the reply as a string
     * @return the requested information
     * @throws Exception in case something goes awry 
     */
    public String getContentFromServer(String service) throws Exception
    {
        LogWriter log = LogWriter.getInstance();
        
        String urlToUse = constructUrl(service);
        URL server;
        StringBuffer result = new StringBuffer();
        
        try
        {
            String beforeProxyHost     = System.getProperty("http.proxyHost"); 
            String beforeProxyPort     = System.getProperty("http.proxyPort"); 
            String beforeNonProxyHosts = System.getProperty("http.nonProxyHosts"); 

            BufferedReader      input        = null;
            
            try
            {
                log.logBasic(toString(), "Connecting to URL: "+urlToUse);

                if (proxyHostname!=null) 
                {
                    System.setProperty("http.proxyHost", proxyHostname);
                    System.setProperty("http.proxyPort", proxyPort);
                    if (nonProxyHosts!=null) System.setProperty("http.nonProxyHosts", nonProxyHosts);
                }
                
                if (username!=null && username.length()>0)
                {
                    Authenticator.setDefault(new Authenticator()
                        {
                            protected PasswordAuthentication getPasswordAuthentication()
                            {
                                return new PasswordAuthentication(username, password!=null ? password.toCharArray() : new char[] {} );
                            }
                        }
                    );
                }

                // Get a stream for the specified URL
                server = new URL(urlToUse);
                URLConnection connection = server.openConnection();
                
                log.logDetailed(toString(), "Start reading reply from webserver.");
    
                // Read the result from the server...
                InputStream inputStream = new BufferedInputStream(connection.getInputStream(), 1000);
                
                input = new BufferedReader(new InputStreamReader( inputStream ));
                
                long bytesRead = 0L;
                String line;
                while ( (line=input.readLine())!=null )
                {
                    result.append(line).append(Const.CR);
                    bytesRead+=line.length();
                }
                
                log.logBasic(toString(), "Finished reading "+bytesRead+" bytes as a response from the webserver");
            }
            catch(MalformedURLException e)
            {
                log.logError(toString(), "The specified URL is not valid ["+urlToUse+"] : "+e.getMessage());
                log.logError(toString(), Const.getStackTracker(e));
            }
            catch(IOException e)
            {
                log.logError(toString(), "I was unable to save the HTTP result to file because of a I/O error: "+e.getMessage());
                log.logError(toString(), Const.getStackTracker(e));
            }
            catch(Exception e)
            {
                log.logError(toString(), "Error getting file from HTTP : "+e.getMessage());
                log.logError(toString(), Const.getStackTracker(e));
            }
            finally
            {
                // Close it all
                try
                {
                    if (input!=null) input.close();
                }
                catch(Exception e)
                {
                    log.logError(toString(), "Unable to close streams : "+e.getMessage());
                    log.logError(toString(), Const.getStackTracker(e));
                }

            }

            // Set the proxy settings back as they were on the system!
            System.setProperty("http.proxyHost", Const.NVL(beforeProxyHost, ""));
            System.setProperty("http.proxyPort", Const.NVL(beforeProxyPort, ""));
            System.setProperty("http.nonProxyHosts", Const.NVL(beforeNonProxyHosts, ""));
            
            // Get the result back...
            return result.toString();
        }
        catch(Exception e)
        {
            throw new Exception("Unable to contact URL ["+urlToUse+"] to get the security reference information.", e);
        }
    }
    
    public SlaveServerStatus getStatus() throws Exception
    {
        String xml = execService(GetStatusHandler.CONTEXT_PATH+"?xml=Y");
        return SlaveServerStatus.fromXML(xml);
    }

    public SlaveServerTransStatus getTransStatus(String transName) throws Exception
    {
        String xml = execService(GetTransStatusHandler.CONTEXT_PATH+"?name="+transName+"&xml=Y");
        return SlaveServerTransStatus.fromXML(xml);
    }
    
    public WebResult stopTransformation(String transName) throws Exception
    {
        String xml = execService(StopTransHandler.CONTEXT_PATH+"?name="+transName+"&xml=Y");
        return WebResult.fromXMLString(xml);
    }
    
    public WebResult startTransformation(String transName) throws Exception
    {
        String xml = execService(StartTransHandler.CONTEXT_PATH+"?name="+transName+"&xml=Y");
        return WebResult.fromXMLString(xml);
    }

    public static SlaveServer findSlaveServer(List slaveServers, String name)
    {
        for (int i=0;i<slaveServers.size();i++)
        {
            SlaveServer slaveServer = (SlaveServer) slaveServers.get(i);
            if (slaveServer.getName()!=null && slaveServer.getName().equalsIgnoreCase(name)) return slaveServer;
        }
        return null;
    }
    
    public static String[] getSlaveServerNames(List slaveServers)
    {
        String[] names = new String[slaveServers.size()];
        for (int i=0;i<slaveServers.size();i++)
        {
            SlaveServer slaveServer = (SlaveServer) slaveServers.get(i);
            names[i] = slaveServer.getName();
        }
        return names;
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

    public long getId()
    {
        return id;
    }

    public void setId(long id)
    {
        this.id = id;
    }

    
}

