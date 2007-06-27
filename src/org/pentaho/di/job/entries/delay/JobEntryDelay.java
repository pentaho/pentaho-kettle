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
 
package org.pentaho.di.job.entries.delay;

import java.util.List;

import org.eclipse.swt.widgets.Shell;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.Result;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.exception.KettleDatabaseException;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleXMLException;
import org.pentaho.di.core.logging.LogWriter;
import org.pentaho.di.core.xml.XMLHandler;
import org.pentaho.di.job.Job;
import org.pentaho.di.job.JobMeta;
import org.pentaho.di.job.entry.JobEntryBase;
import org.pentaho.di.job.entry.JobEntryDialogInterface;
import org.pentaho.di.job.entry.JobEntryInterface;
import org.pentaho.di.repository.Repository;
import org.w3c.dom.Node;


/**
 * Job entry type to sleep for a time. It uses a piece of javascript to do this.
 * 
 * @author Samatar
 * @since 21-02-2007
 */
public class JobEntryDelay extends JobEntryBase implements Cloneable, JobEntryInterface
{
	static private String DEFAULT_MAXIMUM_TIMEOUT  = "0";        
	private String  maximumTimeout;                        // maximum timeout in seconds
	public int scaletime;

	public JobEntryDelay(String n)
	{
		super(n, "");
		setID(-1L);
		setType(JobEntryInterface.TYPE_JOBENTRY_DELAY);
	}

	public JobEntryDelay()
	{
		this("");
	}
	
	public JobEntryDelay(JobEntryBase jeb)
	{
		super(jeb);
	}
    
    public Object clone()
    {
        JobEntryDelay je = (JobEntryDelay) super.clone();
        return je;
    }
	
	public String getXML()
	{
        StringBuffer retval = new StringBuffer(200);
	
		retval.append(super.getXML());
		retval.append("      ").append(XMLHandler.addTagValue("maximumTimeout",  maximumTimeout));
		retval.append("      ").append(XMLHandler.addTagValue("scaletime",       scaletime));

		return retval.toString();
	}
	
	public void loadXML(Node entrynode, List<DatabaseMeta> databases, Repository rep)
		throws KettleXMLException
	{
		try
		{
			super.loadXML(entrynode, databases);
			maximumTimeout = XMLHandler.getTagValue(entrynode, "maximumTimeout");
			scaletime      = Integer.parseInt(XMLHandler.getTagValue(entrynode, "scaletime"));
		}
		catch(Exception e)
		{
			throw new KettleXMLException("Unable to load job entry of type 'delay' from XML node", e);
		}
	}

	public void loadRep(Repository rep, long id_jobentry, List<DatabaseMeta> databases)
		throws KettleException
	{
		try
		{
			super.loadRep(rep, id_jobentry, databases);

			maximumTimeout =      rep.getJobEntryAttributeString(id_jobentry,  "maximumTimeout");
			scaletime      = (int)rep.getJobEntryAttributeInteger(id_jobentry, "scaletime");
		}
		catch(KettleDatabaseException dbe)
		{
			throw new KettleException("Unable to load job entry of type 'delay' from the repository with id_jobentry="+id_jobentry, dbe);
		}
	}

	//
	// Save the attributes of this job entry
	//
	public void saveRep(Repository rep, long id_job)
		throws KettleException
	{
		try
		{
			super.saveRep(rep, id_job);
			
			rep.saveJobEntryAttribute(id_job, getID(), "maximumTimeout", maximumTimeout);
			rep.saveJobEntryAttribute(id_job, getID(), "scaletime",      scaletime);
		}
		catch(KettleDatabaseException dbe)
		{
			throw new KettleException("Unable to save job entry of type 'delay' to the repository for id_job="+id_job, dbe);
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
		LogWriter log = LogWriter.getInstance();
		Result result = previousResult;
		result.setResult( false );
		int Multiple;
		String Waitscale;

		// Scale time
		if (scaletime == 0)
		{
			// Second
			Multiple = 1000;
			Waitscale = Messages.getString("JobEval.SScaleTime.Label");

		}
		else if (scaletime == 1)
		{
			// Minute
			Multiple = 60000;
			Waitscale = Messages.getString("JobEval.MnScaleTime.Label");
		}
		else
		{
			// Hour
			Multiple=3600000;
			Waitscale = Messages.getString("JobEval.HrScaleTime.Label");
		}
		try
		{
			// starttime (in seconds ,Minutes or Hours)
			long timeStart = System.currentTimeMillis() / Multiple;

			long iMaximumTimeout = Const.toInt(getMaximumTimeout(),Const.toInt(DEFAULT_MAXIMUM_TIMEOUT, 0));

			if ( log.isDetailed() )
			{
				log.logDetailed(toString(), Messages.getString("JobEval.LetsWaitFor.Label") + iMaximumTimeout + " " + Waitscale + "...");
			}

			boolean continueLoop = true;
			//
			// Sanity check on some values, and complain on insanity
			//
			if ( iMaximumTimeout < 0 )
			{
				iMaximumTimeout = Const.toInt(DEFAULT_MAXIMUM_TIMEOUT, 0);
				log.logBasic(toString(), Messages.getString("JobEval.MaximunTimeReseted.Label") + iMaximumTimeout+ " " + Waitscale);
			}

			// TODO don't contineously loop, but sleeping would be better.
			while ( continueLoop && !parentJob.isStopped() )
			{
				// Update Time value
				long now = System.currentTimeMillis()/ Multiple;

				// Let's check the limit time
				if ( (iMaximumTimeout > 0) && (now >= (timeStart + iMaximumTimeout)))
				{													
					// We have reached the time limit
					if ( log.isDetailed() )
					{
						log.logDetailed(toString(), Messages.getString("JobEval.WaitTimeIsElapsed.Label"));
					}
					continueLoop = false;
					result.setResult( true );
				}
			}
		}
		catch(Exception e)
		{
			// We get an exception
			result.setResult( false );
			log.logError(toString(), "Error  : "+e.getMessage());
		}
	
		return result;
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
        return new JobEntryDelayDialog(shell,this,jobMeta);
    }
	
	public String getMaximumTimeout() 
	{
		return environmentSubstitute(maximumTimeout);
	}

	public void setMaximumTimeout(String s)
	{
		maximumTimeout = s;
	}
}