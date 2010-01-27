 /* Copyright (c) 2007 Pentaho Corporation.  All rights reserved. 
 * This software was developed by Pentaho Corporation and is provided under the terms 
 * of the GNU Lesser General Public License, Version 2.1. You may not use 
 * this file except in compliance with the license. If you need a copy of the license, 
 * please go to http://www.gnu.org/licenses/lgpl-2.1.txt. The Original Code is Pentaho 
 * Data Integration.  The Initial Developer is Pentaho Corporation.
 *
 * Software distributed under the GNU Lesser Public License is distributed on an "AS IS" 
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or  implied. Please refer to 
 * the license for the specific language governing your rights and limitations.*/
 
package org.pentaho.di.job.entries.writetolog;
import static org.pentaho.di.job.entry.validator.JobEntryValidatorUtils.addOkRemark;

import java.util.List;

import org.pentaho.di.cluster.SlaveServer;
import org.pentaho.di.core.CheckResultInterface;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.Result;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.exception.KettleDatabaseException;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleXMLException;
import org.pentaho.di.core.logging.LogWriter;
import org.pentaho.di.core.xml.XMLHandler;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.job.JobMeta;
import org.pentaho.di.job.entry.JobEntryBase;
import org.pentaho.di.job.entry.JobEntryInterface;
import org.pentaho.di.repository.ObjectId;
import org.pentaho.di.repository.Repository;
import org.w3c.dom.Node;



/**
 * Job entry type to output message to the job log.
 * 
 * @author Samatar
 * @since 08-08-2007
 */

public class JobEntryWriteToLog extends JobEntryBase implements Cloneable, JobEntryInterface
{
	private static Class<?> PKG = JobEntryWriteToLog.class; // for i18n purposes, needed by Translator2!!   $NON-NLS-1$

	private String logmessage;
	public  int     loglevel;
	private String logsubject;

	public JobEntryWriteToLog(String n)
	{
		super(n, "");
		logmessage=null;
		logsubject=null;
	}
	
	
	public JobEntryWriteToLog()
	{
		this("");
	}

    public Object clone()
    {
        JobEntryWriteToLog je = (JobEntryWriteToLog) super.clone();
        return je;
    }
	
	public String getXML()
	{		
        StringBuffer retval = new StringBuffer(200);
	
		retval.append(super.getXML());
		retval.append("      ").append(XMLHandler.addTagValue("logmessage",      logmessage));
		retval.append("      ").append(XMLHandler.addTagValue("loglevel",          LogWriter.getLogLevelDesc(loglevel)));
		retval.append("      ").append(XMLHandler.addTagValue("logsubject",      logsubject));

		return retval.toString();
	}
	
	public void loadXML(Node entrynode, List<DatabaseMeta> databases, List<SlaveServer> slaveServers, Repository rep)
		throws KettleXMLException
	{
		try
		{
			super.loadXML(entrynode, databases, slaveServers);
			logmessage = XMLHandler.getTagValue(entrynode, "logmessage");
			loglevel = LogWriter.getLogLevel( XMLHandler.getTagValue(entrynode, "loglevel"));
			logsubject = XMLHandler.getTagValue(entrynode, "logsubject");
		}
		catch(Exception e)
		{
			throw new KettleXMLException(BaseMessages.getString(PKG, "WriteToLog.Error.UnableToLoadFromXML.Label"), e);
			
		}
	}

	public void loadRep(Repository rep, ObjectId id_jobentry, List<DatabaseMeta> databases, List<SlaveServer> slaveServers) throws KettleException
	{
		try
		{
			logmessage = rep.getJobEntryAttributeString(id_jobentry, "logmessage");
			loglevel = LogWriter.getLogLevel( rep.getJobEntryAttributeString(id_jobentry, "loglevel") );
			logsubject = rep.getJobEntryAttributeString(id_jobentry, "logsubject");
		}
		catch(KettleDatabaseException dbe)
		{
			throw new KettleException(BaseMessages.getString(PKG, "WriteToLog.Error.UnableToLoadFromRepository.Label")+id_jobentry, dbe);
			
		}
	}
	
	// Save the attributes of this job entry
	//
	public void saveRep(Repository rep, ObjectId id_job) throws KettleException
	{
		try
		{
			rep.saveJobEntryAttribute(id_job, getObjectId(), "logmessage", logmessage);
			rep.saveJobEntryAttribute(id_job, getObjectId(), "loglevel", LogWriter.getLogLevelDesc(loglevel));
			rep.saveJobEntryAttribute(id_job, getObjectId(), "logsubject", logsubject);
		}
		catch(KettleDatabaseException dbe)
		{
			throw new KettleException(BaseMessages.getString(PKG, "WriteToLog.Error.UnableToSaveToRepository.Label")+id_job, dbe);
		}
	}


	/**
	 * Output message to job log.
	 */
	public boolean evaluate(Result result)
	{
		try
		{
			loglevel=loglevel+1;
			
			if (loglevel==LogWriter.LOG_LEVEL_ERROR)
			{
				// Output message to log
				// Log level = ERREUR	
				log.logError(Const.CR + getRealLogSubject()+ Const.CR, getRealLogMessage()+ Const.CR);
			}
			else if (loglevel==LogWriter.LOG_LEVEL_MINIMAL)
			{
				// Output message to log
				// Log level = MINIMAL	
				log.logMinimal(Const.CR + getRealLogSubject()+ Const.CR, getRealLogMessage()+ Const.CR);
			}
			else if (loglevel==LogWriter.LOG_LEVEL_BASIC)
			{
				// Output message to log
				// Log level = BASIC	
				log.logBasic(Const.CR + getRealLogSubject()+ Const.CR, getRealLogMessage()+ Const.CR);
			}
			else if (loglevel==LogWriter.LOG_LEVEL_DETAILED)
			{
				// Output message to log
				// Log level = DETAILED	
				log.logDetailed(Const.CR + getRealLogSubject()+ Const.CR, getRealLogMessage()+ Const.CR);
			}
			else if (loglevel==LogWriter.LOG_LEVEL_DEBUG)
			{
				// Output message to log
				// Log level = DEBUG	
				log.logDebug(Const.CR + getRealLogSubject()+ Const.CR, getRealLogMessage()+ Const.CR);
				
			}
			else if (loglevel==LogWriter.LOG_LEVEL_ROWLEVEL)
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
			log.logError(BaseMessages.getString(PKG, "WriteToLog.Error.Label"), BaseMessages.getString(PKG, "WriteToLog.Error.Description") + " : "+e.toString());
			return false;
		}
	
	
	}
	
	/**
	 * Execute this job entry and return the result.
	 * In this case it means, just set the result boolean in the Result class.
	 * @param prev_result The result of the previous execution
	 * @return The Result of the execution.
	 */
	public Result execute(Result prev_result, int nr)
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
    
   /* public JobEntryDialogInterface getDialog(Shell shell,JobEntryInterface jei,JobMeta jobMeta,String jobName,Repository rep) {
        return new JobEntryWriteToLogDialog(shell,this);
    }
*/
	public String getRealLogMessage()
	{
		return environmentSubstitute(Const.NVL(getLogMessage(), ""));
		
	}
	public String getRealLogSubject()
	{
		return 	environmentSubstitute(Const.NVL(getLogSubject(), ""));
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
	
	 @Override
	  public void check(List<CheckResultInterface> remarks, JobMeta jobMeta)
	  {
	    addOkRemark(this, "LogMessage", remarks); //$NON-NLS-1$
	    addOkRemark(this, "LogSubject", remarks); //$NON-NLS-1$
	  }
	
}