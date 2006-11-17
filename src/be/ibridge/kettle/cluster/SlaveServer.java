package be.ibridge.kettle.cluster;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.ByteArrayRequestEntity;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.RequestEntity;
import org.w3c.dom.Node;

import be.ibridge.kettle.core.ChangedFlag;
import be.ibridge.kettle.core.Const;
import be.ibridge.kettle.core.Encr;
import be.ibridge.kettle.core.LogWriter;
import be.ibridge.kettle.core.XMLHandler;
import be.ibridge.kettle.core.util.StringUtil;

public class SlaveServer extends ChangedFlag implements Cloneable
{
    private static LogWriter log = LogWriter.getInstance();
    public static final String PASSWORD_ENCRYPTED_PREFIX = "Encrypted ";
    
    private String hostname;
    private String port;
    private String username;
    private String password;

    private String proxyHostname;
    private String proxyPort;
    private String nonProxyHosts;
    
    public SlaveServer()
    {
    }
    
    public SlaveServer(String hostname, String port, String username, String password, String proxyHostname, String proxyPort, String nonProxyHosts)
    {
        this.hostname = hostname;
        this.port     = port;
        this.username = username;
        this.password = password;

        this.proxyHostname = proxyHostname;
        this.proxyPort = proxyPort;
        this.nonProxyHosts = nonProxyHosts;
    }
    
    public SlaveServer(Node slaveNode)
    {
        this.hostname   = XMLHandler.getTagValue(slaveNode, "hostname");
        this.port       = XMLHandler.getTagValue(slaveNode, "port");
        this.username   = XMLHandler.getTagValue(slaveNode, "username");
        this.password   = XMLHandler.getTagValue(slaveNode, "password");
        if (!Const.isEmpty(password) && password.startsWith(PASSWORD_ENCRYPTED_PREFIX))
        {
            password = Encr.decryptPassword(password.substring(PASSWORD_ENCRYPTED_PREFIX.length()+1));
        }
        this.proxyHostname = XMLHandler.getTagValue(slaveNode, "proxy_hostname");
        this.proxyPort     = XMLHandler.getTagValue(slaveNode, "proxy_port");
        this.nonProxyHosts = XMLHandler.getTagValue(slaveNode, "non_proxy_hosts");
    }

    public String getXML()
    {
        StringBuffer xml = new StringBuffer();
        
        xml.append("<slaveserver>");
        xml.append(XMLHandler.addTagValue("hostname", hostname, false));
        xml.append(XMLHandler.addTagValue("port",     port, false));
        xml.append(XMLHandler.addTagValue("username", username, false));
        xml.append(XMLHandler.addTagValue("proxy_hostname", proxyHostname, false));
        xml.append(XMLHandler.addTagValue("proxy_port", proxyPort, false));
        xml.append(XMLHandler.addTagValue("non_proxy_hosts", nonProxyHosts, false));

        String encrPassword = "";
        List varList = new ArrayList();
        StringUtil.getUsedVariables(password, varList, true);
        if (varList.size()==0 && !Const.isEmpty(password))
        {
            encrPassword = PASSWORD_ENCRYPTED_PREFIX+Encr.encryptPassword(password);
        }
        else
        {
            encrPassword = getPassword();
        }
        xml.append(XMLHandler.addTagValue("password", encrPassword, false));

        xml.append("</slaveserver>");

        return xml.toString();
    }
    
    public Object clone()
    {
        try
        {
            return super.clone();
        }
        catch(Exception e)
        {
            return null;
        }
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
        String portSpec = ":"+port;
        if (Const.isEmpty(port) || port.equals("80"))
        {
            portSpec="";
        }
        return portSpec;
    }
    
    public String constructUrl(String serviceAndArguments)
    {
        return "http://"+hostname+getPortSpecification()+serviceAndArguments;
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
        // post.setContentChunked(true);
        
        // Get HTTP client
        // 
        HttpClient httpclient = new HttpClient();
        
        // Execute request
        // 
        try
        {
            int result = httpclient.executeMethod(post);
            
            // The status code
            log.logDebug(toString(), "Response status code: " + result);
            
            // the response
            String body = post.getResponseBodyAsString(); 
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
    
    public String toString()
    {
        if (!Const.isEmpty(hostname)) return hostname+getPortSpecification();
        return "Slave Server";
    }

}

