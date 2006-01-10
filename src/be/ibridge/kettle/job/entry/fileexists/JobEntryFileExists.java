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
 
package be.ibridge.kettle.job.entry.fileexists;
import java.io.File;
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
 * This defines an SQL job entry.
 * 
 * @author Matt
 * @since 05-11-2003
 *
 */

public class JobEntryFileExists extends JobEntryBase implements JobEntryInterface
{
	private String filename;
	
	public JobEntryFileExists(String n)
	{
		super(n, "");
		filename=null;
		setID(-1L);
		setType(JobEntryInterface.TYPE_JOBENTRY_FILE_EXISTS);
	}

	public JobEntryFileExists()
	{
		this("");
	}

	public JobEntryFileExists(JobEntryBase jeb)
	{
		super(jeb);
	}

	public String getXML()
	{
		String retval ="";
		
		retval+=super.getXML();
		
		retval+="      "+XMLHandler.addTagValue("filename",   filename);
		
		return retval;
	}
	
	public void loadXML(Node entrynode, ArrayList databases)
		throws KettleXMLException
	{
		try
		{
			super.loadXML(entrynode, databases);
			filename      = XMLHandler.getTagValue(entrynode, "filename");
		}
		catch(KettleXMLException xe)
		{
			throw new KettleXMLException("Unable to load file exists job entry from XML node", xe);
		}
	}

	public void loadRep(Repository rep, long id_jobentry, ArrayList databases)
		throws KettleException
	{
		try
		{
			super.loadRep(rep, id_jobentry, databases);
			filename = rep.getJobEntryAttributeString(id_jobentry, "filename");
		}
		catch(KettleException dbe)
		{
			throw new KettleException("Unable to load job entry for type file exists from the repository for id_jobentry="+id_jobentry, dbe);
		}
	}
	
	public void saveRep(Repository rep, long id_job)
		throws KettleException
	{
		try
		{
			super.saveRep(rep, id_job);
			
			rep.saveJobEntryAttribute(id_job, getID(), "filename", filename);
		}
		catch(KettleDatabaseException dbe)
		{
			throw new KettleException("unable to save jobentry of type 'file exists' to the repository for id_job="+id_job, dbe);
		}
	}

	public void setFilename(String filename)
	{
		this.filename = filename;
	}
	
	public String getFilename()
	{
		return filename;
	}
	
	public Result execute(Result prev_result, int nr, Repository rep, Job parentJob)
	{
		LogWriter log = LogWriter.getInstance();
		Result result = new Result(nr);
		result.setResult( false );
	
		if (filename!=null)
		{
			File file = new File(Const.replEnv(filename));
			if (file.exists() && file.canRead())
			{
				log.logDetailed(toString(), "File ["+filename+"] exists.");
				result.setResult( true );
			}
			else
			{
				log.logDetailed(toString(), "File ["+filename+"] doesn't exist!");
			}
		}
		else
		{
			result.setNrErrors(1);
			log.logError(toString(), "No filename is defined.");
		}
		
		return result;
	}

	public boolean evaluates()
	{
		return true;
	}
}
