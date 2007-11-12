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

package org.pentaho.di.job.entries.waitforfile;

import static org.pentaho.di.job.entry.validator.AndValidator.putValidators;
import static org.pentaho.di.job.entry.validator.JobEntryValidatorUtils.andValidator;
import static org.pentaho.di.job.entry.validator.JobEntryValidatorUtils.integerValidator;
import static org.pentaho.di.job.entry.validator.JobEntryValidatorUtils.notBlankValidator;

import java.io.IOException;
import java.util.List;

import org.apache.commons.vfs.FileObject;
import org.pentaho.di.cluster.SlaveServer;
import org.pentaho.di.core.CheckResultInterface;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.Result;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.exception.KettleDatabaseException;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleXMLException;
import org.pentaho.di.core.logging.LogWriter;
import org.pentaho.di.core.vfs.KettleVFS;
import org.pentaho.di.core.xml.XMLHandler;
import org.pentaho.di.job.Job;
import org.pentaho.di.job.JobEntryType;
import org.pentaho.di.job.JobMeta;
import org.pentaho.di.job.entry.JobEntryBase;
import org.pentaho.di.job.entry.JobEntryInterface;
import org.pentaho.di.repository.Repository;
import org.pentaho.di.resource.ResourceEntry;
import org.pentaho.di.resource.ResourceReference;
import org.pentaho.di.resource.ResourceEntry.ResourceType;
import org.w3c.dom.Node;

/**
 * This defines a 'wait for file' job entry. Its use is to wait for a file to
 * appear.
 *
 * @author Sven Boden
 * @since 10-02-2007
 *
 */
public class JobEntryWaitForFile extends JobEntryBase implements Cloneable, JobEntryInterface
{
	private String  filename;
	private String  maximumTimeout;      // maximum timeout in seconds
	private String  checkCycleTime;      // cycle time in seconds
	private boolean successOnTimeout;
	private boolean fileSizeCheck;

	static private String DEFAULT_MAXIMUM_TIMEOUT  = "0";        // infinite timeout
	static private String DEFAULT_CHECK_CYCLE_TIME = "60";       // 1 minute

	public JobEntryWaitForFile(String n)
	{
		super(n, "");
		filename = null;
		maximumTimeout   = DEFAULT_MAXIMUM_TIMEOUT;
		checkCycleTime   = DEFAULT_CHECK_CYCLE_TIME;
		successOnTimeout = false;
		fileSizeCheck    = false;
		setID(-1L);
		setJobEntryType(JobEntryType.WAIT_FOR_FILE);
	}

	public JobEntryWaitForFile()
	{
		this("");
	}

	public JobEntryWaitForFile(JobEntryBase jeb)
	{
		super(jeb);
	}

    public Object clone()
    {
        JobEntryWaitForFile je = (JobEntryWaitForFile) super.clone();
        return je;
    }

	public String getXML()
	{
        StringBuffer retval = new StringBuffer(50);

		retval.append(super.getXML());
		retval.append("      ").append(XMLHandler.addTagValue("filename", filename));
		retval.append("      ").append(XMLHandler.addTagValue("maximum_timeout", maximumTimeout));
		retval.append("      ").append(XMLHandler.addTagValue("check_cycle_time", checkCycleTime));
		retval.append("      ").append(XMLHandler.addTagValue("success_on_timeout", successOnTimeout));
		retval.append("      ").append(XMLHandler.addTagValue("file_size_check", fileSizeCheck));

		return retval.toString();
	}

	public void loadXML(Node entrynode, List<DatabaseMeta> databases, List<SlaveServer> slaveServers, Repository rep)
		throws KettleXMLException
	{
		try
		{
			super.loadXML(entrynode, databases, slaveServers);
			filename = XMLHandler.getTagValue(entrynode, "filename");
			maximumTimeout = XMLHandler.getTagValue(entrynode, "maximum_timeout");
			checkCycleTime = XMLHandler.getTagValue(entrynode, "check_cycle_time");
			successOnTimeout = "Y".equalsIgnoreCase(XMLHandler.getTagValue(entrynode, "success_on_timeout"));
			fileSizeCheck = "Y".equalsIgnoreCase(XMLHandler.getTagValue(entrynode, "file_size_check"));
		}
		catch(KettleXMLException xe)
		{
			throw new KettleXMLException("Unable to load job entry of type 'wait for file' from XML node", xe);
		}
	}

	public void loadRep(Repository rep, long id_jobentry, List<DatabaseMeta> databases, List<SlaveServer> slaveServers)
		throws KettleException
	{
		try
		{
			super.loadRep(rep, id_jobentry, databases, slaveServers);
			filename = rep.getJobEntryAttributeString(id_jobentry, "filename");
			maximumTimeout = rep.getJobEntryAttributeString(id_jobentry, "maximum_timeout");
			checkCycleTime = rep.getJobEntryAttributeString(id_jobentry, "check_cycle_time");
			successOnTimeout = rep.getJobEntryAttributeBoolean(id_jobentry, "success_on_timeout");
			fileSizeCheck = rep.getJobEntryAttributeBoolean(id_jobentry, "file_size_check");
		}
		catch(KettleException dbe)
		{
			throw new KettleException("Unable to load job entry of type 'wait for file' from the repository for id_jobentry="+id_jobentry, dbe);
		}
	}

	public void saveRep(Repository rep, long id_job)
		throws KettleException
	{
		try
		{
			super.saveRep(rep, id_job);

			rep.saveJobEntryAttribute(id_job, getID(), "filename", filename);
			rep.saveJobEntryAttribute(id_job, getID(), "maximum_timeout", maximumTimeout);
			rep.saveJobEntryAttribute(id_job, getID(), "check_cycle_time", checkCycleTime);
            rep.saveJobEntryAttribute(id_job, getID(), "success_on_timeout", successOnTimeout);
            rep.saveJobEntryAttribute(id_job, getID(), "file_size_check", fileSizeCheck);
		}
		catch(KettleDatabaseException dbe)
		{
			throw new KettleException("Unable to save job entry of type 'wait for file' to the repository for id_job="+id_job, dbe);
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

    public String getRealFilename()
    {
        return environmentSubstitute(getFilename());
    }

    public Result execute(Result previousResult, int nr, Repository rep, Job parentJob)
    {
    	LogWriter log = LogWriter.getInstance();
    	Result result = previousResult;
    	result.setResult( false );

    	// starttime (in seconds)
    	long timeStart = System.currentTimeMillis() / 1000;

    	if (filename!=null)
    	{
    		String realFilename = getRealFilename();
    		try
    		{
    			FileObject fileObject = null;

    			fileObject = KettleVFS.getFileObject(realFilename);

    			long iMaximumTimeout = Const.toInt(getMaximumTimeout(),
    					Const.toInt(DEFAULT_MAXIMUM_TIMEOUT, 0));
    			long iCycleTime = Const.toInt(getCheckCycleTime(),
    					Const.toInt(DEFAULT_CHECK_CYCLE_TIME, 0));

    			//
    			// Sanity check on some values, and complain on insanity
    			//
    			if ( iMaximumTimeout < 0 )
    			{
    				iMaximumTimeout = Const.toInt(DEFAULT_MAXIMUM_TIMEOUT, 0);
    				log.logBasic(toString(), "Maximum timeout invalid, reset to " + iMaximumTimeout);
    			}

    			if ( iCycleTime < 1 )
    			{
    				// If lower than 1 set to the default
    				iCycleTime = Const.toInt(DEFAULT_CHECK_CYCLE_TIME, 1);
    				log.logBasic(toString(), "Check cycle time invalid, reset to " + iCycleTime);
    			}

    			if ( iMaximumTimeout == 0 )
    			{
    				log.logBasic(toString(), "Waiting indefinitely for file [" +
    						realFilename + "]");
    			}
    			else
    			{
    				log.logBasic(toString(), "Waiting " + iMaximumTimeout + " seconds for file [" +
    						realFilename + "]");
    			}

    			boolean continueLoop = true;
    			while ( continueLoop && !parentJob.isStopped() )
    			{
        			fileObject = KettleVFS.getFileObject(realFilename);

    				if ( fileObject.exists() )
    				{
    					// file exists, we're happy to exit
    					log.logBasic(toString(), "Detected file [" + realFilename + "] within timeout");
    					result.setResult( true );
    					continueLoop = false;
    				}
    				else
    				{
    					long now = System.currentTimeMillis() / 1000;

    					if ( (iMaximumTimeout > 0) &&
    							(now > (timeStart + iMaximumTimeout)))
    					{
    						continueLoop = false;

    						// file doesn't exist after timeout, either true or false
    						if ( isSuccessOnTimeout() )
    						{
    							log.logBasic(toString(), "Didn't detect file [" + realFilename + "] before timeout, success");
    							result.setResult( true );
    						}
    						else
    						{
    							log.logBasic(toString(), "Didn't detect file [" + realFilename + "] before timeout, failure");
    							result.setResult( false );
    						}
    					}

    					// sleep algorithm
    					long sleepTime = 0;

    					if ( iMaximumTimeout == 0 )
    					{
    						sleepTime = iCycleTime;
    					}
    					else
    					{
    						if ( (now + iCycleTime) < (timeStart + iMaximumTimeout) )
    						{
    							sleepTime = iCycleTime;
    						}
    						else
    						{
    							sleepTime = iCycleTime - ((now + iCycleTime) -
    									(timeStart + iMaximumTimeout));
    						}
    					}

    					try {
    						if ( sleepTime > 0 )
    						{
    							if ( log.isDetailed() )
    							{
    								log.logDetailed(toString(), "Sleeping " + sleepTime + " seconds before next check for file [" + realFilename + "]");
    							}
    							Thread.sleep(sleepTime * 1000);
    						}
    					} catch (InterruptedException e) {
    						// something strange happened
    						result.setResult( false );
    						continueLoop = false;
    					}
    				}
    			}

    			if ( !parentJob.isStopped() &&
    					fileObject.exists() &&
    					isFileSizeCheck() )
    			{
    				long oldSize = -1;
    				long newSize = fileObject.getContent().getSize();

    				log.logDetailed(toString(), "File [" + realFilename + "] is " + newSize + " bytes long");
    				log.logBasic(toString(), "Waiting until file [" + realFilename + "] stops growing for " + iCycleTime + " seconds");
    				while ( oldSize != newSize && !parentJob.isStopped() )
    				{
    					try {
    						if ( log.isDetailed() )
    						{
    							log.logDetailed(toString(), "Sleeping " + iCycleTime + " seconds, waiting for file [" + realFilename + "] to stop growing");
    						}
    						Thread.sleep(iCycleTime * 1000);
    					} catch (InterruptedException e) {
    						// something strange happened
    						result.setResult( false );
    						continueLoop = false;
    					}
    					oldSize = newSize;
    					newSize = fileObject.getContent().getSize();
    					if ( log.isDetailed() )
    					{
    						log.logDetailed(toString(), "File [" + realFilename + "] is " + newSize + " bytes long");
    					}
    				}
    				log.logBasic(toString(), "Stopped waiting for file [" + realFilename + "] to stop growing");
    			}

    			if ( parentJob.isStopped() )
    			{
    				result.setResult( false );
    			}
    		}
    		catch ( IOException e )
    		{
    			log.logBasic(toString(), "Exception while waiting for file [" + realFilename + "] to stop growing: " + e.getMessage());
    		}
    	}
    	else
    	{
    		log.logError(toString(), "No filename is defined.");
    	}

    	return result;
    }

	public boolean evaluates()
	{
		return true;
	}

	public boolean isSuccessOnTimeout() {
		return successOnTimeout;
	}

	public void setSuccessOnTimeout(boolean successOnTimeout) {
		this.successOnTimeout = successOnTimeout;
	}

	public String getCheckCycleTime() {
		return checkCycleTime;
	}

	public void setCheckCycleTime(String checkCycleTime) {
		this.checkCycleTime = checkCycleTime;
	}

	public String getMaximumTimeout() {
		return maximumTimeout;
	}

	public void setMaximumTimeout(String maximumTimeout) {
		this.maximumTimeout = maximumTimeout;
	}

	public boolean isFileSizeCheck() {
		return fileSizeCheck;
	}

	public void setFileSizeCheck(boolean fileSizeCheck) {
		this.fileSizeCheck = fileSizeCheck;
	}

  public List<ResourceReference> getResourceDependencies(JobMeta jobMeta) {
    List<ResourceReference> references = super.getResourceDependencies(jobMeta);
    if (!Const.isEmpty(filename)) {
      String realFileName = jobMeta.environmentSubstitute(filename);
      ResourceReference reference = new ResourceReference(this);
      reference.getEntries().add( new ResourceEntry(realFileName, ResourceType.FILE));
      references.add(reference);
    }
    return references;
  }

  @Override
  public void check(List<CheckResultInterface> remarks, JobMeta jobMeta)
  {
    andValidator().validate(this, "filename", remarks, putValidators(notBlankValidator())); //$NON-NLS-1$
    andValidator().validate(this, "maximumTimeout", remarks, putValidators(integerValidator())); //$NON-NLS-1$
    andValidator().validate(this, "checkCycleTime", remarks, putValidators(integerValidator())); //$NON-NLS-1$
  }

}