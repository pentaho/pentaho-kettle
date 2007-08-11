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
 
package be.ibridge.kettle.job.entry.writetolog;
import java.util.ArrayList;

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
 * Job entry type to output message to the job log.
 * 
 * @author Samatar
 * @since 08-08-2007
 */

public class JobEntryWriteToLog extends JobEntryBase implements Cloneable, JobEntryInterface
{
	private String logmessage;
	public  int     loglevel;
	private String logsubject;

	public JobEntryWriteToLog(String n, String scr)
	{
		super(n, "");
		logmessage=null;
		logsubject=null;
		setType(JobEntryInterface.TYPE_WRITE_TO_LOG);
	}

	public JobEntryWriteToLog()
	{
		this("", "");
	}
	
	public JobEntryWriteToLog(JobEntryBase jeb)
	{
		super(jeb);
	}
    
    public Object clone()
    {
        JobEntryWriteToLog je = (JobEntryWriteToLog) super.clone();
        return je;
    }
	
	public String getXML()
	{
		
        StringBuffer retval = new StringBuffer();
	
		retval.append(super.getXML());
		retval.append("      ").append(XMLHandler.addTagValue("logmessage",      logmessage));
		retval.append("      ").append(XMLHandler.addTagValue("loglevel",          LogWriter.getLogLevelDesc(loglevel)));
		retval.append("      ").append(XMLHandler.addTagValue("logsubject",      logsubject));

		return retval.toString();
	}
	
	public void loadXML(Node entrynode, ArrayList databases, Repository rep)
		throws KettleXMLException
	{
		try
		{
			super.loadXML(entrynode, databases);
			logmessage = XMLHandler.getTagValue(entrynode, "logmessage");
			loglevel = LogWriter.getLogLevel( XMLHandler.getTagValue(entrynode, "loglevel"));
			logsubject = XMLHandler.getTagValue(entrynode, "logsubject");
		}
		catch(Exception e)
		{
			throw new KettleXMLException(Messages.getString("WriteToLog.Error.UnableToLoadFromXML.Label"), e);
			
		}
	}

	public void loadRep(Repository rep, long id_jobentry, ArrayList databases)
		throws KettleException
	{
		try
		{
			super.loadRep(rep, id_jobentry, databases);

			logmessage = rep.getJobEntryAttributeString(id_jobentry, "logmessage");
			loglevel = LogWriter.getLogLevel( rep.getJobEntryAttributeString(id_jobentry, "loglevel") );
			logsubject = rep.getJobEntryAttributeString(id_jobentry, "logsubject");
		}
		catch(KettleDatabaseException dbe)
		{
			throw new KettleException(Messages.getString("WriteToLog.Error.UnableToLoadFromRepository.Label")+id_jobentry, dbe);
			
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
			
			rep.saveJobEntryAttribute(id_job, getID(), "logmessage", logmessage);
			rep.saveJobEntryAttribute(id_job, getID(), "loglevel", LogWriter.getLogLevelDesc(loglevel));
			rep.saveJobEntryAttribute(id_job, getID(), "logsubject", logsubject);

		}
		catch(KettleDatabaseException dbe)
		{
			throw new KettleException(Messages.getString("WriteToLog.Error.UnableToSaveToRepository.Label")+id_job, dbe);
		}
	}


	/**
	 * Output message to job log.
	 */
	public boolean evaluate(Result result)
	{
		LogWriter log = LogWriter.getInstance();
		
			
		try
		{
			
			log.logBasic("mon log", loglevel + "");
			log.logBasic(" log", log.LOG_LEVEL_ERROR + "");
				
			loglevel=loglevel+1;
			
			if (loglevel==log.LOG_LEVEL_ERROR)
			{
				// Output message to log
				// Log level = ERREUR	
				log.logError(Const.CR + getRealLogSubject()+ Const.CR, getRealLogMessage()+ Const.CR);
			}
			else if (loglevel==log.LOG_LEVEL_MINIMAL)
			{
				// Output message to log
				// Log level = MINIMAL	
				log.logMinimal(Const.CR + getRealLogSubject()+ Const.CR, getRealLogMessage()+ Const.CR);
			}
			else if (loglevel==log.LOG_LEVEL_BASIC)
			{
				// Output message to log
				// Log level = BASIC	
				log.logBasic(Const.CR + getRealLogSubject()+ Const.CR, getRealLogMessage()+ Const.CR);
			}
			else if (loglevel==log.LOG_LEVEL_DETAILED)
			{
				// Output message to log
				// Log level = DETAILED	
				log.logDetailed(Const.CR + getRealLogSubject()+ Const.CR, getRealLogMessage()+ Const.CR);
			}
			else if (loglevel==log.LOG_LEVEL_DEBUG)
			{
				// Output message to log
				// Log level = DEBUG	
				log.logDebug(Const.CR + getRealLogSubject()+ Const.CR, getRealLogMessage()+ Const.CR);
				
			}
			else if (loglevel==log.LOG_LEVEL_ROWLEVEL)
			{
				// Output message to log
				// Log level = ROWLEVEL	
				log.logRowlevel(Const.CR + getRealLogSubject()+ Const.CR, getRealLogMessage()+ Const.CR);
			}



			return true;
			
			

			
					
		}
		catch(Exception e)
		{
			result.setNrErrors(1);
			log.logError(Messages.getString("WriteToLog.Error.Label"), Messages.getString("WriteToLog.Error.Description") + " : "+e.toString());
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
        return new JobEntryWriteToLogDialog(shell,this);
    }

	public String getRealLogMessage()
	{
		return StringUtil.environmentSubstitute(getLogMessage());
	}
	public String getRealLogSubject()
	{
		return StringUtil.environmentSubstitute(getLogSubject());
	}
	
	public String getLogMessage()
	{
		if (logmessage == null)
		{
			logmessage="";
		}
		return logmessage;
	
		
	}
	public String getLogSubject()
	{
		if (logsubject == null)
		{
			logsubject="";
		}
		return logsubject;
	
		
	}
	public void setLogMessage(String s)
	{
		
		logmessage=s;
	
	}
	public void setLogSubject(String logsubjectin)
	{
		
		logsubject=logsubjectin;
	
	}
	
}