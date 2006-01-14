/**********************************************************************
 **                                                                   **
 **               This code belongs to the KETTLE project.            **
 **                                                                   **
 ** Kettle, from version 2.2 on, is released into the public domain   **
 ** under the Lesser GNU Public License (LGPL).                       **
 **                                                                   **
 ** For more details, please read the document LICENSE.txt, included  **
 ** in this project                                                   **
 **                                                                   **
 ** http://www.kettle.be                                              **
 ** info@kettle.be                                                    **
 **                                                                   **
 **********************************************************************/

package be.ibridge.kettle.job.entry.http;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Authenticator;
import java.net.MalformedURLException;
import java.net.PasswordAuthentication;
import java.net.URL;
import java.net.URLConnection;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.w3c.dom.Node;

import be.ibridge.kettle.core.Const;
import be.ibridge.kettle.core.Encr;
import be.ibridge.kettle.core.LogWriter;
import be.ibridge.kettle.core.Result;
import be.ibridge.kettle.core.Row;
import be.ibridge.kettle.core.XMLHandler;
import be.ibridge.kettle.core.exception.KettleDatabaseException;
import be.ibridge.kettle.core.exception.KettleException;
import be.ibridge.kettle.core.exception.KettleXMLException;
import be.ibridge.kettle.core.value.Value;
import be.ibridge.kettle.job.Job;
import be.ibridge.kettle.job.entry.JobEntryBase;
import be.ibridge.kettle.job.entry.JobEntryInterface;
import be.ibridge.kettle.repository.Repository;

/**
 * This defines an HTTP job entry.
 * 
 * TODO: allow for Environment variables to be set.
 * TODO: show tooltip of actual URL and files that is sent.
 * 
 * @author Matt
 * @since 05-11-2003
 * 
 */

public class JobEntryHTTP extends JobEntryBase implements JobEntryInterface
{
    private static final String URL_FIELDNAME = "URL";

    // Base info
    private String              url;
    private String              targetFilename;
    private boolean             fileAppended;
    private boolean             dateTimeAdded;
    private String              targetFilenameExtention;

    // Send file content to server?
    private String              uploadFilename;

    // The fieldname that contains the URL
    // Get it from a previous transformation with Result.
    private String              urlFieldname;
    private boolean             runForEveryRow;

    // Proxy settings
    private String              proxyHostname;
    private int                 proxyPort;
    private String              nonProxyHosts;
    private String              username;
    private String              password;

    public String getNonProxyHosts()
    {
        return nonProxyHosts;
    }

    public void setNonProxyHosts(String nonProxyHosts)
    {
        this.nonProxyHosts = nonProxyHosts;
    }

    public String getPassword()
    {
        return password;
    }

    public void setPassword(String password)
    {
        this.password = password;
    }

    public String getProxyHostname()
    {
        return proxyHostname;
    }

    public void setProxyHostname(String proxyHostname)
    {
        this.proxyHostname = proxyHostname;
    }

    public int getProxyPort()
    {
        return proxyPort;
    }

    public void setProxyPort(int proxyPort)
    {
        this.proxyPort = proxyPort;
    }

    public String getUsername()
    {
        return username;
    }

    public void setUsername(String username)
    {
        this.username = username;
    }

    public JobEntryHTTP(String n)
    {
        super(n, "");
        url = null;
        setID(-1L);
        setType(JobEntryInterface.TYPE_JOBENTRY_HTTP);
    }

    public JobEntryHTTP()
    {
        this("");
    }

    public JobEntryHTTP(JobEntryBase jeb)
    {
        super(jeb);
    }

    public String getXML()
    {
        String retval = "";

        retval += super.getXML();

        retval += "      " + XMLHandler.addTagValue("url", url);
        retval += "      " + XMLHandler.addTagValue("targetfilename", targetFilename);
        retval += "      " + XMLHandler.addTagValue("file_appended", fileAppended);
        retval += "      " + XMLHandler.addTagValue("date_time_added", dateTimeAdded);
        retval += "      " + XMLHandler.addTagValue("targetfilename_extention", targetFilenameExtention);
        retval += "      " + XMLHandler.addTagValue("uploadfilename", uploadFilename);

        retval += "      " + XMLHandler.addTagValue("url_fieldname", urlFieldname);
        retval += "      " + XMLHandler.addTagValue("run_every_row", runForEveryRow);

        retval += "      " + XMLHandler.addTagValue("username", username);
        if (password != null && password.length() > 0)
        {
            retval += "      " + XMLHandler.addTagValue("password", "Encrypted: " + Encr.encryptPassword(password));
        }

        retval += "      " + XMLHandler.addTagValue("proxy_host", proxyHostname);
        retval += "      " + XMLHandler.addTagValue("proxy_port", proxyPort);
        retval += "      " + XMLHandler.addTagValue("non_proxy_hosts", nonProxyHosts);

        return retval;
    }

    public void loadXML(Node entrynode, ArrayList databases) throws KettleXMLException
    {
        try
        {
            super.loadXML(entrynode, databases);
            url = XMLHandler.getTagValue(entrynode, "url");
            targetFilename = XMLHandler.getTagValue(entrynode, "targetfilename");
            fileAppended = "Y".equalsIgnoreCase(XMLHandler.getTagValue(entrynode, "file_appended"));
            dateTimeAdded = "Y".equalsIgnoreCase( XMLHandler.getTagValue(entrynode, "date_time_added") );
            targetFilenameExtention = XMLHandler.getTagValue(entrynode, "targetfilename_extention");

            uploadFilename = XMLHandler.getTagValue(entrynode, "uploadfilename");

            urlFieldname = XMLHandler.getTagValue(entrynode, "url_fieldname");
            runForEveryRow = "Y".equalsIgnoreCase(XMLHandler.getTagValue(entrynode, "run_every_row"));

            username = XMLHandler.getTagValue(entrynode, "username");
            password = XMLHandler.getTagValue(entrynode, "password");
            if (password != null && password.startsWith("Encrypted: "))
            {
                password = Encr.decryptPassword(password.substring(11));
            }

            proxyHostname = XMLHandler.getTagValue(entrynode, "proxy_host");
            proxyPort = Const.toInt(XMLHandler.getTagValue(entrynode, "proxy_port"), 0);
            nonProxyHosts = XMLHandler.getTagValue(entrynode, "non_proxy_hosts");
        }
        catch (KettleXMLException xe)
        {
            throw new KettleXMLException("Unable to load HTTP job entry from XML node", xe);
        }
    }

    public void loadRep(Repository rep, long id_jobentry, ArrayList databases) throws KettleException
    {
        try
        {
            super.loadRep(rep, id_jobentry, databases);
            url = rep.getJobEntryAttributeString(id_jobentry, "url");
            targetFilename = rep.getJobEntryAttributeString(id_jobentry, "targetfilename");
            fileAppended = rep.getJobEntryAttributeBoolean(id_jobentry, "file_appended");
            dateTimeAdded = "Y".equalsIgnoreCase( rep.getJobEntryAttributeString(id_jobentry, "date_time_added") );
            targetFilenameExtention = rep.getJobEntryAttributeString(id_jobentry, "targetfilename_extention");

            uploadFilename = rep.getJobEntryAttributeString(id_jobentry, "uploadfilename");

            urlFieldname = rep.getJobEntryAttributeString(id_jobentry, "url_fieldname");
            runForEveryRow = rep.getJobEntryAttributeBoolean(id_jobentry, "run_every_row");

            username = rep.getJobEntryAttributeString(id_jobentry, "username");
            password = rep.getJobEntryAttributeString(id_jobentry, "password");
            if (password != null && password.startsWith("Encrypted: "))
            {
                password = Encr.decryptPassword(password.substring(11));
            }

            proxyHostname = rep.getJobEntryAttributeString(id_jobentry, "proxy_host");
            proxyPort = (int) rep.getJobEntryAttributeInteger(id_jobentry, "proxy_port");
            nonProxyHosts = rep.getJobEntryAttributeString(id_jobentry, "non_proxy_hosts");
        }
        catch (KettleException dbe)
        {
            throw new KettleException("Unable to load job entry for type HTTP from the repository for id_jobentry=" + id_jobentry, dbe);
        }
    }

    public void saveRep(Repository rep, long id_job) throws KettleException
    {
        try
        {
            super.saveRep(rep, id_job);

            rep.saveJobEntryAttribute(id_job, getID(), "url", url);
            rep.saveJobEntryAttribute(id_job, getID(), "targetfilename", targetFilename);
            rep.saveJobEntryAttribute(id_job, getID(), "file_appended", fileAppended);
            rep.saveJobEntryAttribute(id_job, getID(), "date_time_added", dateTimeAdded);
            rep.saveJobEntryAttribute(id_job, getID(), "targetfilename_extention", targetFilenameExtention);

            rep.saveJobEntryAttribute(id_job, getID(), "uploadfilename", uploadFilename);

            rep.saveJobEntryAttribute(id_job, getID(), "url_fieldname", urlFieldname);
            rep.saveJobEntryAttribute(id_job, getID(), "run_every_row", runForEveryRow);

            rep.saveJobEntryAttribute(id_job, getID(), "username", username);
            if (password != null && password.length() > 0)
            {
                rep.saveJobEntryAttribute(id_job, getID(), "password", "Encrypted: " + Encr.encryptPassword(password));
            }

            rep.saveJobEntryAttribute(id_job, getID(), "proxy_host", proxyHostname);
            rep.saveJobEntryAttribute(id_job, getID(), "proxy_port", proxyPort);
            rep.saveJobEntryAttribute(id_job, getID(), "non_proxy_hosts", nonProxyHosts);
        }
        catch (KettleDatabaseException dbe)
        {
            throw new KettleException("unable to save jobentry of type HTTP to the repository for id_job=" + id_job, dbe);
        }
    }

    /**
     * @return Returns the URL.
     */
    public String getUrl()
    {
        return url;
    }

    /**
     * @param url The URL to set.
     */
    public void setUrl(String url)
    {
        this.url = url;
    }

    /**
     * @return Returns the target filename.
     */
    public String getTargetFilename()
    {
        return targetFilename;
    }

    /**
     * @param targetFilename The target filename to set.
     */
    public void setTargetFilename(String targetFilename)
    {
        this.targetFilename = targetFilename;
    }

    public synchronized Result execute(Result prev_result, int nr, Repository rep, Job parentJob)
	{
		LogWriter log = LogWriter.getInstance();

		Result result = new Result(nr);
		result.setResult( false );

		log.logBasic(toString(), "Start of HTTP job entry.");

        // Get previous result rows...
        List resultRows;
        String urlFieldnameToUse = URL_FIELDNAME;

        if (runForEveryRow)
        {
            resultRows = prev_result.getRows();
            if (resultRows==null)
            {
                result.setNrErrors(1);
                log.logError(toString(), "Unable to get result from previous job entry : can't continue.");
                return result;
            }
        }
        else
        {
            resultRows = new ArrayList();
            Row row = new Row();
            row.addValue(new Value(urlFieldnameToUse, url));
            resultRows.add(row);
            System.out.println("Added one row to rows: "+row);
        }

        
        URL server = null;

        String beforeProxyHost     = System.getProperty("http.proxyHost"); 
        String beforeProxyPort     = System.getProperty("http.proxyPort"); 
        String beforeNonProxyHosts = System.getProperty("http.nonProxyHosts"); 

        for (int i=0;i<resultRows.size() && result.getNrErrors()==0;i++)
        {
            Row row = (Row)resultRows.get(i);
            
            FileOutputStream    outputFile   = null;
            OutputStream        uploadStream = null;
            BufferedInputStream fileStream   = null;
            BufferedReader      input        = null;
            
            try
            {
                String urlToUse = row.getString(urlFieldnameToUse, "");
                
                log.logBasic(toString(), "Connecting to URL: "+urlToUse);

                if (proxyHostname!=null) 
                {
                    System.setProperty("http.proxyHost", proxyHostname);
                    System.setProperty("http.proxyPort", ""+proxyPort);
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
                
                String targetFile = targetFilename;
                if (dateTimeAdded)
                {
                    SimpleDateFormat daf = new SimpleDateFormat();
                    Date now = new Date();
                    
                    daf.applyPattern("yyyMMdd");
                    targetFile+="_"+daf.format(now);
                    daf.applyPattern("HHmmss");
                    targetFile+="_"+daf.format(now);
                    
                    if (targetFilenameExtention!=null && targetFilenameExtention.length()>0)
                    {
                        targetFile+="."+targetFilenameExtention;
                    }
                }
                
                // Create the output File...
                outputFile = new FileOutputStream(new File(targetFile), fileAppended);
                
                // Get a stream for the specified URL
    		    server = new URL(urlToUse);
                URLConnection connection = server.openConnection();
                
                // See if we need to send a file over?
                if (uploadFilename!=null && uploadFilename.length()>0)
                {
                    log.logDetailed(toString(), "Start sending content of file ["+uploadFilename+"] to server.");
                    
                    // Grab an output stream to upload data to web server
                    uploadStream = connection.getOutputStream();
                    
                    fileStream = new BufferedInputStream(new FileInputStream(new File(uploadFilename)));
                    
                    int c;
                    while ( (c=fileStream.read())>=0)
                    {
                        uploadStream.write(c);
                    }
                    
                    // Close upload and file
                    uploadStream.close(); uploadStream=null;
                    fileStream.close(); fileStream=null;
                    
                    log.logDetailed(toString(), "Finished sending content to server.");
                }
    
                log.logDetailed(toString(), "Start reading reply from webserver.");
    
                // Read the result from the server...
                input = new BufferedReader(new InputStreamReader( connection.getInputStream() ));
                
                long bytesRead = 0L;
                String line;
                while ( (line=input.readLine())!=null )
                {
                    line+=Const.CR;
                    bytesRead+=line.length();
                    outputFile.write(line.getBytes());
                }
                
                log.logBasic(toString(), "Finished writing "+bytesRead+" bytes to result file ["+targetFile+"]");
                result.setResult( true );
            }
            catch(MalformedURLException e)
            {
                result.setNrErrors(1);
                e.printStackTrace();
                log.logError(toString(), "The specified URL is not valid ["+url+"] : "+e.getMessage());
            }
            catch(IOException e)
            {
                result.setNrErrors(1);
                e.printStackTrace();
                log.logError(toString(), "I was unable to save the HTTP result to file because of a I/O error: "+e.getMessage());
            }
            catch(Exception e)
            {
                result.setNrErrors(1);
                e.printStackTrace();
                log.logError(toString(), "Error getting file from HTTP : "+e.getMessage());
            }
            finally
            {
                // Close it all
                try
                {
                    if (uploadStream!=null) uploadStream.close(); // just to make sure
                    if (fileStream!=null) fileStream.close(); // just to make sure
                    
                    if (input!=null) input.close();
                    if (outputFile!=null) outputFile.close();
                }
                catch(Exception e)
                {
                    log.logError(toString(), "Unable to close streams : "+e.getMessage());
                    result.setNrErrors(1);
                }

            }

        }
        
        // Set the proxy settings back as they were on the system!
        System.setProperty("http.proxyHost", Const.NVL(beforeProxyHost, ""));
        System.setProperty("http.proxyPort", Const.NVL(beforeProxyPort, ""));
        System.setProperty("http.nonProxyHosts", Const.NVL(beforeNonProxyHosts, ""));
		
		return result;
	}    

    public boolean evaluates()
    {
        return true;
    }

    public String getUploadFilename()
    {
        return uploadFilename;
    }

    public void setUploadFilename(String uploadFilename)
    {
        this.uploadFilename = uploadFilename;
    }

    /**
     * @return Returns the getFieldname.
     */
    public String getUrlFieldname()
    {
        return urlFieldname;
    }

    /**
     * @param getFieldname The getFieldname to set.
     */
    public void setUrlFieldname(String getFieldname)
    {
        this.urlFieldname = getFieldname;
    }

    /**
     * @return Returns the runForEveryRow.
     */
    public boolean isRunForEveryRow()
    {
        return runForEveryRow;
    }

    /**
     * @param runForEveryRow The runForEveryRow to set.
     */
    public void setRunForEveryRow(boolean runForEveryRow)
    {
        this.runForEveryRow = runForEveryRow;
    }

    /**
     * @return Returns the fileAppended.
     */
    public boolean isFileAppended()
    {
        return fileAppended;
    }

    /**
     * @param fileAppended The fileAppended to set.
     */
    public void setFileAppended(boolean fileAppended)
    {
        this.fileAppended = fileAppended;
    }

    /**
     * @return Returns the dateTimeAdded.
     */
    public boolean isDateTimeAdded()
    {
        return dateTimeAdded;
    }

    /**
     * @param dateTimeAdded The dateTimeAdded to set.
     */
    public void setDateTimeAdded(boolean dateTimeAdded)
    {
        this.dateTimeAdded = dateTimeAdded;
    }

    /**
     * @return Returns the uploadFilenameExtention.
     */
    public String getTargetFilenameExtention()
    {
        return targetFilenameExtention;
    }

    /**
     * @param uploadFilenameExtention The uploadFilenameExtention to set.
     */
    public void setTargetFilenameExtention(String uploadFilenameExtention)
    {
        this.targetFilenameExtention = uploadFilenameExtention;
    }
}
