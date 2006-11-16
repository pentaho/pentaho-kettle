package be.ibridge.kettle.cluster;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Authenticator;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.PasswordAuthentication;
import java.net.URL;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.ByteArrayRequestEntity;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.RequestEntity;

import be.ibridge.kettle.core.ChangedFlag;
import be.ibridge.kettle.core.Const;
import be.ibridge.kettle.core.LogWriter;

public class SlaveServer extends ChangedFlag implements Cloneable
{
    private static LogWriter log = LogWriter.getInstance();
    
    private String serviceUrl;
    private String username;
    private String password;

    private String proxyHostname;
    private String proxyPort;
    private String nonProxyHosts;
    
    public SlaveServer()
    {
    }
    
    public SlaveServer(String urlString, String username, String password, String proxyHostname, String proxyPort, String nonProxyHosts)
    {
        this.serviceUrl = urlString;
        this.username = username;
        this.password = password;

        this.proxyHostname = proxyHostname;
        this.proxyPort = proxyPort;
        this.nonProxyHosts = nonProxyHosts;
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
    
    public String getServiceUrl()
    {
        return serviceUrl;
    }
    
    public void setServiceUrl(String urlString)
    {
        this.serviceUrl = urlString;
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
    
    
    public String sendXML(String xml) throws Exception
    {
        // The content
        // 
        byte[] content = xml.getBytes(Const.XML_ENCODING);
        
        // Prepare HTTP post
        // 
        PostMethod post = new PostMethod(serviceUrl);
        
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
            // Display status code
            System.out.println("Response status code: " + result);
            // Display response
            System.out.println("Response body: ");
            return post.getResponseBodyAsString();
        }
        finally
        {
            // Release current connection to the connection pool once you are done
            post.releaseConnection();
        }
        
    }
    
    /**
     * Send the XML over to the specified HTTP server and get back the result
     * @return the result from the webserver, error, OK messages, etc.
     * @throws Exception in case there is an error during the post operation.
     */
    public String sendXMLOld(String xml) throws Exception
    {
        URL server = null;
        OutputStream uploadStream = null;
        BufferedInputStream fileStream = null;
        BufferedReader input = null;

        String beforeProxyHost     = System.getProperty("http.proxyHost"); 
        String beforeProxyPort     = System.getProperty("http.proxyPort"); 
        String beforeNonProxyHosts = System.getProperty("http.nonProxyHosts"); 

        try
        {
            log.logBasic(toString(), "Connecting to URL: " + serviceUrl);

            if (!Const.isEmpty(proxyHostname)) 
            {
                System.setProperty("http.proxyHost", proxyHostname);
                System.setProperty("http.proxyPort", ""+proxyPort);
                if (nonProxyHosts!=null) System.setProperty("http.nonProxyHosts", nonProxyHosts);
            }
            
            if (username != null && username.length() > 0)
            {
                Authenticator.setDefault(new Authenticator()
                {
                    protected PasswordAuthentication getPasswordAuthentication()
                    {
                        return new PasswordAuthentication(username, password != null ? password.toCharArray() : new char[] {});
                    }
                });
            }
            
            // The content to send?
            byte[] content = xml.getBytes();

            // Get a stream for the specified URL
            server = new URL(serviceUrl);
            HttpURLConnection connection = (HttpURLConnection)server.openConnection();

            connection.setRequestMethod("POST");
            connection.setDoOutput(true);
            connection.setDoInput(true);
            connection.setUseCaches(false);
            connection.setRequestProperty("Content-Type", "text/xml");
            connection.setRequestProperty("Transfer-Encoding", "chunked" );
            
            // Send the XML over to the server
            log.logDetailed(toString(), "Start sending content of XML to server.");

            // Grab an output stream to upload data to web server
            uploadStream = connection.getOutputStream();

            // send over the content
            uploadStream.write(content);

            // Close upload
            uploadStream.flush();
            uploadStream.close();
            uploadStream = null;

            log.logDetailed(toString(), "Finished sending XML to server.");

            log.logDetailed(toString(), "Start reading reply from webserver.");

            // Read the result from the server...
            input = new BufferedReader(new InputStreamReader(connection.getInputStream()));

            StringBuffer result = new StringBuffer();

            long bytesRead = 0L;
            String line;
            while ((line = input.readLine()) != null)
            {
                line += Const.CR;
                result.append(line);
                bytesRead += line.length();
            }
            input.close();

            log.logBasic(toString(), "Finished reading " + bytesRead + " bytes from the webserver as a result.");

            return result.toString();
        }
        catch (MalformedURLException e)
        {
            throw new Exception("The specified URL is not valid [" + serviceUrl + "] : ", e);
        }
        catch (Exception e)
        {
            throw new Exception("Error posting XML to server: ", e);
        }
        finally
        {
            // Close it all
            try
            {
                if (uploadStream != null) uploadStream.close(); // just to make sure
                if (fileStream != null) fileStream.close(); // just to make sure

                if (input != null) input.close();
            }
            catch (Exception e)
            {
                throw new Exception("Unable to close streams", e);
            }

            // Set the proxy settings back as they were on the system!
            System.setProperty("http.proxyHost", Const.NVL(beforeProxyHost, ""));
            System.setProperty("http.proxyPort", Const.NVL(beforeProxyPort, ""));
            System.setProperty("http.nonProxyHosts", Const.NVL(beforeNonProxyHosts, ""));
        }

    }
    
    public String toString()
    {
        if (!Const.isEmpty(serviceUrl)) return serviceUrl;
        return "Slave Server";
    }

}
