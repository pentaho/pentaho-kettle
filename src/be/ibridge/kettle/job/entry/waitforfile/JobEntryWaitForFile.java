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
 
package be.ibridge.kettle.job.entry.waitforfile;

import java.io.IOException;
import java.util.ArrayList;

import org.apache.commons.vfs.FileObject;
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
import be.ibridge.kettle.core.vfs.KettleVFS;
import be.ibridge.kettle.job.Job;
import be.ibridge.kettle.job.JobMeta;
import be.ibridge.kettle.job.entry.JobEntryBase;
import be.ibridge.kettle.job.entry.JobEntryDialogInterface;
import be.ibridge.kettle.job.entry.JobEntryInterface;
import be.ibridge.kettle.repository.Repository;


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
		setType(JobEntryInterface.TYPE_JOBENTRY_WAIT_FOR_FILE);
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
	
	public void loadXML(Node entrynode, ArrayList databases, Repository rep)
		throws KettleXMLException
	{
		try
		{
			super.loadXML(entrynode, databases);
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

	public void loadRep(Repository rep, long id_jobentry, ArrayList databases)
		throws KettleException
	{
		try
		{
			super.loadRep(rep, id_jobentry, databases);
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
        return StringUtil.environmentSubstitute(getFilename());
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
    								log.logDetailed(toString(), "Sleeping " + sleepTime + " seconds before next check for file [" +
    										realFilename + "]");							
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
    
    public JobEntryDialogInterface getDialog(Shell shell,JobEntryInterface jei,JobMeta jobMeta,String jobName,Repository rep) {
        return new JobEntryWaitForFileDialog(shell,this,jobMeta);
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
}