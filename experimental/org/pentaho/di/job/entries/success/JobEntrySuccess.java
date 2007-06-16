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
 
package org.pentaho.di.job.entries.success;
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

import be.ibridge.kettle.core.exception.KettleDatabaseException;
import be.ibridge.kettle.core.exception.KettleException;
import be.ibridge.kettle.core.exception.KettleXMLException;



/**
 * Job entry type to success a job.
 * 
 * @author Samatar
 * @since 12-02-2007
 */
public class JobEntrySuccess extends JobEntryBase implements Cloneable, JobEntryInterface
{
	
	public JobEntrySuccess(String n, String scr)
	{
		super(n, "");
		setType(JobEntryInterface.TYPE_JOBENTRY_SUCCESS);
	}

	public JobEntrySuccess()
	{
		this("", "");
	}
	
	public JobEntrySuccess(JobEntryBase jeb)
	{
		super(jeb);
	}
    
    public Object clone()
    {
        JobEntrySuccess je = (JobEntrySuccess) super.clone();
        return je;
    }
	
	public String getXML()
	{
        StringBuffer retval = new StringBuffer();
	
		retval.append(super.getXML());

		return retval.toString();
	}
	
	public void loadXML(Node entrynode, ArrayList databases, Repository rep)
		throws KettleXMLException
	{
		try
		{
			super.loadXML(entrynode, databases);
		}
		catch(Exception e)
		{
			throw new KettleXMLException(Messages.getString("JobEntrySuccess.Meta.UnableToLoadFromXML"), e);
		}
	}

	public void loadRep(Repository rep, long id_jobentry, ArrayList databases)
		throws KettleException
	{
		try
		{
			super.loadRep(rep, id_jobentry, databases);
		}
		catch(KettleDatabaseException dbe)
		{
			throw new KettleException(Messages.getString("JobEntrySuccess.Meta.UnableToLoadFromRep")+id_jobentry, dbe);
		
					
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

		}
		catch(KettleDatabaseException dbe)
		{
			throw new KettleException(Messages.getString("JobEntrySuccess.Meta.UnableToSaveToRep")+id_job, dbe);
		}
	}

	public boolean evaluate(Result result)
	{
	
		return true;
		
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
        return new JobEntrySuccessDialog(shell,this);
    }
	
}