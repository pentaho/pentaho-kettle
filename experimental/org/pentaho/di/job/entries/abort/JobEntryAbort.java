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
 
package org.pentaho.di.job.entries.abort;
import java.util.ArrayList;

import org.eclipse.swt.widgets.Shell;
import org.pentaho.di.core.Result;
import org.pentaho.di.job.Job;
import org.pentaho.di.job.JobMeta;
import org.pentaho.di.job.entry.JobEntryBase;
import org.pentaho.di.job.entry.JobEntryDialogInterface;
import org.pentaho.di.job.entry.JobEntryInterface;
import org.pentaho.di.repository.Repository;
import org.w3c.dom.Node;

import be.ibridge.kettle.core.LogWriter;
import be.ibridge.kettle.core.XMLHandler;
import be.ibridge.kettle.core.exception.KettleDatabaseException;
import be.ibridge.kettle.core.exception.KettleException;
import be.ibridge.kettle.core.exception.KettleXMLException;
import org.pentaho.di.core.util.StringUtil;



/**
 * Job entry type to abort a job.
 * 
 * @author Samatar
 * @since 12-02-2007
 */
public class JobEntryAbort extends JobEntryBase implements Cloneable, JobEntryInterface
{
	private String messageabort;
	
	public JobEntryAbort(String n, String scr)
	{
		super(n, "");
		messageabort=null;
		setType(JobEntryInterface.TYPE_JOBENTRY_ABORT);
	}

	public JobEntryAbort()
	{
		this("", "");
	}
	
	public JobEntryAbort(JobEntryBase jeb)
	{
		super(jeb);
	}
    
    public Object clone()
    {
        JobEntryAbort je = (JobEntryAbort) super.clone();
        return je;
    }
	
	public String getXML()
	{
        StringBuffer retval = new StringBuffer();
	
		retval.append(super.getXML());
		retval.append("      ").append(XMLHandler.addTagValue("message",   messageabort));

		return retval.toString();
	}
	
	public void loadXML(Node entrynode, ArrayList databases, Repository rep)
		throws KettleXMLException
	{
		try
		{
			super.loadXML(entrynode, databases);
			messageabort = XMLHandler.getTagValue(entrynode, "message");
		}
		catch(Exception e)
		{
			throw new KettleXMLException("Unable to load job entry of type 'Abort' from XML node", e);
		}
	}

	public void loadRep(Repository rep, long id_jobentry, ArrayList databases)
		throws KettleException
	{
		try
		{
			super.loadRep(rep, id_jobentry, databases);
			messageabort = rep.getJobEntryAttributeString(id_jobentry, "message");
		}
		catch(KettleDatabaseException dbe)
		{
			throw new KettleException("Unable to load job entry of type 'Abort' from the repository with id_jobentry="+id_jobentry, dbe);
		}
	}
	
	// Save the attributes of this job entry
	//
	public void saveRep(Repository rep, long id_job)
		throws KettleException
	{
		try
		{
			super.saveRep(rep, id_job);
			rep.saveJobEntryAttribute(id_job, getID(), "message", messageabort);

		}
		catch(KettleDatabaseException dbe)
		{
			throw new KettleException("Unable to save job entry of type 'Abort' to the repository for id_job="+id_job, dbe);
		}
	}

	public boolean evaluate(Result result)
	{
		LogWriter log = LogWriter.getInstance();
		String Returnmessage=null;
		String RealMessageabort=StringUtil.environmentSubstitute(getMessageabort());	

		try
		{
			// Return False
			if (RealMessageabort==null)
			{
				Returnmessage = Messages.getString("JobEntryAbort.Meta.CheckResult.Label");
			}
			else
			{
				Returnmessage = RealMessageabort;

			}			
			log.logError(toString(), Returnmessage);	
			result.setNrErrors(1);
			return false;
		}
		catch(Exception e)
		{
			result.setNrErrors(1);
			log.logError(toString(), Messages.getString("JobEntryAbort.Meta.CheckResult.CoundntExecute") +e.toString());
			return false;
		}
	}
	
	/**
	 * Execute this job entry and return the result.
	 * In this case it means, just set the result boolean in the Result class.
	 * @param previousResult The result of the previous execution
	 * @return The Result of the execution.
	 */
	public Result execute(Result previousResult, int nr, Repository rep, Job parentJob)
	{
		previousResult.setResult( evaluate(previousResult) );
		
		return previousResult;
	}
	
	public boolean resetErrorsBeforeExecution()
	{
		// we should be able to evaluate the errors in
		// the previous jobentry.
	    return false;
	}
	
	public boolean evaluates()
	{
		return true;
	}
	
	public boolean isUnconditional()
	{
		return false;
	}
    
    public JobEntryDialogInterface getDialog(Shell shell,JobEntryInterface jei,JobMeta jobMeta,String jobName,Repository rep) {
        return new JobEntryAbortDialog(shell,this);
    }
	public void setMessageabort(String messageabort)
	{
		this.messageabort = messageabort;
	}
	
	public String getMessageabort()
	{
		return messageabort;
	}
}