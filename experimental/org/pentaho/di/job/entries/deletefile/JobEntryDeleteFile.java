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
 
package org.pentaho.di.job.entries.deletefile;

import java.io.IOException;
import java.util.ArrayList;

import org.apache.commons.vfs.FileObject;
import org.eclipse.swt.widgets.Shell;
import org.pentaho.di.core.Result;
import org.pentaho.di.job.Job;
import org.pentaho.di.job.JobMeta;
import org.pentaho.di.job.entry.JobEntryBase;
import org.pentaho.di.job.entry.JobEntryDialogInterface;
import org.pentaho.di.job.entry.JobEntryInterface;
import org.pentaho.di.repository.Repository;
import org.w3c.dom.Node;

import org.pentaho.di.core.logging.LogWriter;
import org.pentaho.di.core.xml.XMLHandler;
import org.pentaho.di.core.exception.KettleDatabaseException;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleXMLException;
import org.pentaho.di.core.util.StringUtil;
import org.pentaho.di.core.vfs.KettleVFS;




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
	
	public Result execute(Result previousResult, int nr, Repository rep, Job parentJob)
	{
		LogWriter log = LogWriter.getInstance();
		Result result = previousResult;
		result.setResult( false );
	
		if (filename!=null)
		{
            String realFilename = getRealFilename(); 
            
            FileObject fileObject = null;
            try {
            	fileObject = KettleVFS.getFileObject(realFilename);
			
				if ( ! fileObject.exists() )
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
					// Here gc() is explicitly called if e.g. createfile is used in the same
					// job for the same file. The problem is that after creating the file the
					// file object is not properly garbaged collected and thus the file cannot
					// be deleted anymore. This is a known problem in the JVM.
					System.gc();
					
				    boolean deleted = fileObject.delete();
				    if ( ! deleted )
				    {
						log.logError(toString(), "Could not delete file ["+realFilename+"].");
						result.setResult( false );
						result.setNrErrors(1);									    	
				    }
					log.logBasic(toString(), "File ["+realFilename+"] deleted!");
					result.setResult( true );
				}
			} 
            catch (IOException e) {
				log.logError(toString(), "Could not delete file ["+realFilename+"], exception: " + e.getMessage());
				result.setResult( false );
				result.setNrErrors(1);					
			}
            finally {
            	if ( fileObject != null )
            	{
            		try  {
            		     fileObject.close();
            		}
            		catch ( IOException ex ) {};
            	}
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