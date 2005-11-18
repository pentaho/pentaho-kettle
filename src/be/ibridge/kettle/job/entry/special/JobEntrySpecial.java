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
 
package be.ibridge.kettle.job.entry.special;
import java.util.ArrayList;

import org.w3c.dom.Node;

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
 * This class can contain a few special job entries such as Start and Dummy.
 * 
 * @author Matt 
 * @since 05-11-2003
 *
 */

public class JobEntrySpecial extends JobEntryBase implements JobEntryInterface
{
	private boolean start;
	private boolean dummy;

	public JobEntrySpecial()
	{
		this(null, false, false);
	}
	
	public JobEntrySpecial(String name, boolean start, boolean dummy)
	{
		super(name, "");
		this.start = start;
		this.dummy = dummy;
		setType(JobEntryInterface.TYPE_JOBENTRY_SPECIAL);
	}
	
	public JobEntrySpecial(JobEntryBase jeb)
	{
		super(jeb);
	}

	public String getXML()
	{
		String retval ="";
		
		retval+=super.getXML();
		
		retval+="      "+XMLHandler.addTagValue("start",      start);
		retval+="      "+XMLHandler.addTagValue("dummy",      dummy);

		return retval;
	}

	public void loadXML(Node entrynode, ArrayList databases)
		throws KettleXMLException
	{
		try
		{
			super.loadXML(entrynode, databases);
			start = "Y".equalsIgnoreCase(XMLHandler.getTagValue(entrynode, "start"));
			dummy = "Y".equalsIgnoreCase(XMLHandler.getTagValue(entrynode, "dummy"));
		}
		catch(KettleException e)
		{
			throw new KettleXMLException("Unable to load special job entry from XML node", e);
		}
	}

	public void loadRep(Repository rep, long id_jobentry, ArrayList databases)
		throws KettleException
	{
		try
		{
			super.loadRep(rep, id_jobentry, databases);
			
			start = rep.getJobEntryAttributeBoolean(id_jobentry, "start");
			dummy = rep.getJobEntryAttributeBoolean(id_jobentry, "dummy");
		}
		catch(KettleDatabaseException dbe)
		{
			throw new KettleException("Unable to load job entry of type special from the repository for id_jobentry="+id_jobentry, dbe);
		}
	}
	
	// Save the attributes of this job entry
	//
	public void saveRep(Repository rep, long id_job)
		throws KettleException
	{
		super.saveRep(rep, id_job);

		try
		{
			rep.saveJobEntryAttribute(id_job, getID(), "start", start);
			rep.saveJobEntryAttribute(id_job, getID(), "dummy", dummy);
		}
		catch(KettleDatabaseException dbe)
		{
			throw new KettleException("Unable to save job entry of type special to the repository with id_job="+id_job, dbe);
		}
	}

	public boolean isStart()
	{
		return start;
	}

	public boolean isDummy()
	{
		return dummy;
	}
	
	public Result execute(Result prev_result, int nr, Repository rep, Job parentJob)
	{
		Result result = prev_result;

		if (isStart())
		{
			result = new Result(nr);
			result.setResult( true );
		}
		else
		if (isDummy())
		{
			result = new Result(nr);
			result.setResult( prev_result.getResult() );
		}
		return result;
	}
	
	public boolean evaluates()
	{
		return false;
	}

	public boolean isUnconditional()
	{
		return true;
	}

}
