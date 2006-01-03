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
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

import org.w3c.dom.Node;

import be.ibridge.kettle.core.Const;
import be.ibridge.kettle.core.LogWriter;
import be.ibridge.kettle.core.Result;
import be.ibridge.kettle.core.XMLHandler;
import be.ibridge.kettle.core.exception.KettleDatabaseException;
import be.ibridge.kettle.core.exception.KettleException;
import be.ibridge.kettle.core.exception.KettleXMLException;
import be.ibridge.kettle.job.Job;
import be.ibridge.kettle.job.entry.JobEntryBase;
import be.ibridge.kettle.job.entry.JobEntryInterface;
import be.ibridge.kettle.repository.Repository;

/**
 * This defines an FTP job entry.
 * 
 * @author Matt
 * @since 05-11-2003
 *
 */

public class JobEntryHTTP extends JobEntryBase implements JobEntryInterface
{
	private String url;
	private String targetFilename;
	
	public JobEntryHTTP(String n)
	{
		super(n, "");
		url=null;
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
		String retval ="";
		
		retval+=super.getXML();
		
		retval+="      "+XMLHandler.addTagValue("url",   url);
		retval+="      "+XMLHandler.addTagValue("targetfilename", targetFilename);
		
		return retval;
	}
	
	public void loadXML(Node entrynode, ArrayList databases)
		throws KettleXMLException
	{
		try
		{
			super.loadXML(entrynode, databases);
			url      = XMLHandler.getTagValue(entrynode, "url");
			targetFilename = XMLHandler.getTagValue(entrynode, "targetfilename");
		}
		catch(KettleXMLException xe)
		{
			throw new KettleXMLException("Unable to load HTTP job entry from XML node", xe);
		}
	}

	public void loadRep(Repository rep, long id_jobentry, ArrayList databases)
		throws KettleException
	{
		try
		{
			super.loadRep(rep, id_jobentry, databases);
			url            = rep.getJobEntryAttributeString(id_jobentry, "url");
			targetFilename = rep.getJobEntryAttributeString(id_jobentry, "targetfilename");
		}
		catch(KettleException dbe)
		{
			throw new KettleException("Unable to load job entry for type HTTP from the repository for id_jobentry="+id_jobentry, dbe);
		}
	}
	
	public void saveRep(Repository rep, long id_job)
		throws KettleException
	{
		try
		{
			super.saveRep(rep, id_job);
			
			rep.saveJobEntryAttribute(id_job, getID(), "url",             url);
			rep.saveJobEntryAttribute(id_job, getID(), "targetfilename",  targetFilename);
		}
		catch(KettleDatabaseException dbe)
		{
			throw new KettleException("unable to save jobentry of type HTTP to the repository for id_job="+id_job, dbe);
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
	
	public Result execute(Result prev_result, int nr, Repository rep, Job parentJob)
	{
		LogWriter log = LogWriter.getInstance();

		Result result = new Result(nr);
		result.setResult( false );

		log.logBasic(toString(), "Start of HTTP job entry ("+url+")");

        URL server = null;
        
		try
		{
            // Create the output File...
            FileOutputStream output = new FileOutputStream(new File(targetFilename));
            
            // Get a stream for the specified URL
		    server = new URL(url);
            BufferedReader input = new BufferedReader(new InputStreamReader( server.openStream() ));
            
            String line;
            while ( (line=input.readLine())!=null )
            {
                output.write((line+Const.CR).getBytes());
            }
            input.close();
            output.close();
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
		
		return result;
	}

	public boolean evaluates()
	{
		return true;
	}
}
