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
 
package be.ibridge.kettle.job.entry.abort;
import java.util.ArrayList;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Shell;
import org.w3c.dom.Node;

import be.ibridge.kettle.core.Const;
import be.ibridge.kettle.core.LogWriter;
import be.ibridge.kettle.core.Result;
import be.ibridge.kettle.core.XMLHandler;
import be.ibridge.kettle.core.exception.KettleDatabaseException;
import be.ibridge.kettle.core.exception.KettleException;
import be.ibridge.kettle.core.exception.KettleXMLException;
import be.ibridge.kettle.core.util.StringUtil;
import be.ibridge.kettle.job.Job;
import be.ibridge.kettle.job.JobMeta;
import be.ibridge.kettle.job.entry.JobEntryBase;
import be.ibridge.kettle.job.entry.JobEntryDialogInterface;
import be.ibridge.kettle.job.entry.JobEntryInterface;
import be.ibridge.kettle.repository.Repository;


/**
 * Job entry type to display a message box.
 * It uses a piece of javascript to do this.
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
		retval.append("      ").append(XMLHandler.addTagValue("messageabort",   messageabort));

		return retval.toString();
	}
	
	public void loadXML(Node entrynode, ArrayList databases, Repository rep)
		throws KettleXMLException
	{
		try
		{
			super.loadXML(entrynode, databases);
			messageabort = XMLHandler.getTagValue(entrynode, "messageabort");
		}
		catch(Exception e)
		{
			throw new KettleXMLException("Unable to load job entry of type '" + Messages.getString("JobEntryAbort.Meta.Type") + "' from XML node", e);
		}
	}

	public void loadRep(Repository rep, long id_jobentry, ArrayList databases)
		throws KettleException
	{
		try
		{
			super.loadRep(rep, id_jobentry, databases);
			messageabort = rep.getJobEntryAttributeString(id_jobentry, "messageabort");

		}
		catch(KettleDatabaseException dbe)
		{
			throw new KettleException("Unable to load job entry of type '" + Messages.getString("JobEntryAbort.Meta.Type") + "' from the repository with id_jobentry="+id_jobentry, dbe);
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
			rep.saveJobEntryAttribute(id_job, getID(), "messageabort", messageabort);

		}
		catch(KettleDatabaseException dbe)
		{
			throw new KettleException("Unable to save job entry of type '" + Messages.getString("JobEntryAbort.Meta.Type") + "' to the repository for id_job="+id_job, dbe);
		}
	}


	/**
	 * Display the Message Box.
	 */
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
	 * @param prev_result The result of the previous execution
	 * @return The Result of the execution.
	 */
	public Result execute(Result prev_result, int nr, Repository rep, Job parentJob)
	{
		prev_result.setResult( evaluate(prev_result) );
		
		return prev_result;
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