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
 
package be.ibridge.kettle.job.entry.deletefile;

import java.io.File;
import java.util.ArrayList;

import org.eclipse.swt.widgets.Shell;
import org.w3c.dom.Node;

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
 * This defines a 'delete file' job entry. Its main use would be to delete 
 * trigger files, but it will delete any file.
 * 
 * @author Sven Boden
 * @since 10-02-2007
 *
 */
public class JobEntryDeleteFile extends JobEntryBase implements Cloneable, JobEntryInterface
{
	private String filename;
	private boolean failIfFileNotExists;
	
	public JobEntryDeleteFile(String n)
	{
		super(n, "");
		filename=null;
		failIfFileNotExists=false;
		setID(-1L);
		setType(JobEntryInterface.TYPE_JOBENTRY_DELETE_FILE);
	}

	public JobEntryDeleteFile()
	{
		this("");
	}

	public JobEntryDeleteFile(JobEntryBase jeb)
	{
		super(jeb);
	}

    public Object clone()
    {
        JobEntryDeleteFile je = (JobEntryDeleteFile) super.clone();
        return je;
    }
    
	public String getXML()
	{
        StringBuffer retval = new StringBuffer(50);
		
		retval.append(super.getXML());		
		retval.append("      ").append(XMLHandler.addTagValue("filename",   filename));
		retval.append("      ").append(XMLHandler.addTagValue("fail_if_file_not_exists", failIfFileNotExists));
		
		return retval.toString();
	}
	
	public void loadXML(Node entrynode, ArrayList databases, Repository rep)
		throws KettleXMLException
	{
		try
		{
			super.loadXML(entrynode, databases);
			filename = XMLHandler.getTagValue(entrynode, "filename");
			failIfFileNotExists = "Y".equalsIgnoreCase(XMLHandler.getTagValue(entrynode, "fail_if_file_not_exists"));
		}
		catch(KettleXMLException xe)
		{
			throw new KettleXMLException("Unable to load job entry of type 'delete file' from XML node", xe);
		}
	}

	public void loadRep(Repository rep, long id_jobentry, ArrayList databases)
		throws KettleException
	{
		try
		{
			super.loadRep(rep, id_jobentry, databases);
			filename = rep.getJobEntryAttributeString(id_jobentry, "filename");
			failIfFileNotExists = rep.getJobEntryAttributeBoolean(id_jobentry, "fail_if_file_not_exists");
		}
		catch(KettleException dbe)
		{
			throw new KettleException("Unable to load job entry of type 'delete file' from the repository for id_jobentry="+id_jobentry, dbe);
		}
	}
	
	public void saveRep(Repository rep, long id_job)
		throws KettleException
	{
		try
		{
			super.saveRep(rep, id_job);
			
			rep.saveJobEntryAttribute(id_job, getID(), "filename", filename);
            rep.saveJobEntryAttribute(id_job, getID(), "fail_if_file_not_exists", failIfFileNotExists);
		}
		catch(KettleDatabaseException dbe)
		{
			throw new KettleException("Unable to save job entry of type 'delete file' to the repository for id_job="+id_job, dbe);
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
	
	public Result execute(Result prev_result, int nr, Repository rep, Job parentJob)
	{
		LogWriter log = LogWriter.getInstance();
		Result result = new Result(nr);
		result.setResult( false );
	
		if (filename!=null)
		{
            String realFilename = getRealFilename(); 
			File file = new File(realFilename);
			if ( ! file.exists() )
			{
				if ( isFailIfFileNotExists() )
				{
					// File doesn't exist and fail flag is on.
				    result.setResult( false );
				    log.logError(toString(), "File ["+realFilename+"] doesn't exist, failing.");
				}
				else
				{
					// File already deleted, no reason to try to delete it
				    result.setResult( true );
				    log.logBasic(toString(), "File ["+realFilename+"] already deleted.");
				}
			}
			else
			{
				try     
				{
				    boolean deleted = file.delete();
				    if ( ! deleted )
				    {
						log.logError(toString(), "Could not delete file ["+realFilename+"], aborting.");
						result.setResult( false );
						result.setNrErrors(1);									    	
				    }
				}
				catch (Exception e)
				{
					log.logError(toString(), "Could not delete file ["+realFilename+"], aborting.");
					result.setResult( false );
					result.setNrErrors(1);					
				}		 		 			
				log.logBasic(toString(), "File ["+realFilename+"] deleted!");
				result.setResult( true );
			}
		}
		else
		{			
			log.logError(toString(), "No filename is defined.");
		}
		
		return result;
	}

	public JobEntryDialogInterface getDialog(Shell shell,JobEntryInterface jei,JobMeta jobMeta,String jobName,Repository rep) {
        return new JobEntryDeleteFileDialog(shell,this,jobMeta);
    }

	public boolean isFailIfFileNotExists() {
		return failIfFileNotExists;
	}

	public void setFailIfFileNotExists(boolean failIfFileExists) {
		this.failIfFileNotExists = failIfFileExists;
	}
	
	public boolean evaluates()
	{
		return true;
	}	
}