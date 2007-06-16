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
 
package org.pentaho.di.job.entries.fileexists;
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

import be.ibridge.kettle.core.LogWriter;
import be.ibridge.kettle.core.XMLHandler;
import be.ibridge.kettle.core.exception.KettleDatabaseException;
import be.ibridge.kettle.core.exception.KettleException;
import be.ibridge.kettle.core.exception.KettleXMLException;
import org.pentaho.di.core.util.StringUtil;
import be.ibridge.kettle.core.vfs.KettleVFS;




/**
 * This defines an SQL job entry.
 * 
 * @author Matt
 * @since 05-11-2003
 *
 */

public class JobEntryFileExists extends JobEntryBase implements Cloneable, JobEntryInterface
{
	private String filename;
	
	public JobEntryFileExists(String n)
	{
		super(n, "");
		filename=null;
		setID(-1L);
		setType(JobEntryInterface.TYPE_JOBENTRY_FILE_EXISTS);
	}

	public JobEntryFileExists()
	{
		this("");
	}

	public JobEntryFileExists(JobEntryBase jeb)
	{
		super(jeb);
	}

    public Object clone()
    {
        JobEntryFileExists je = (JobEntryFileExists) super.clone();
        return je;
    }
    
	public String getXML()
	{
        StringBuffer retval = new StringBuffer();
		
		retval.append(super.getXML());		
		retval.append("      ").append(XMLHandler.addTagValue("filename",   filename));
		
		return retval.toString();
	}
	
	public void loadXML(Node entrynode, ArrayList databases, Repository rep)
		throws KettleXMLException
	{
		try
		{
			super.loadXML(entrynode, databases);
			filename      = XMLHandler.getTagValue(entrynode, "filename");
		}
		catch(KettleXMLException xe)
		{
			throw new KettleXMLException("Unable to load job entry of type 'file exists' from XML node", xe);
		}
	}

	public void loadRep(Repository rep, long id_jobentry, ArrayList databases)
		throws KettleException
	{
		try
		{
			super.loadRep(rep, id_jobentry, databases);
			filename = rep.getJobEntryAttributeString(id_jobentry, "filename");
		}
		catch(KettleException dbe)
		{
			throw new KettleException("Unable to load job entry of type 'file exists' exists from the repository for id_jobentry="+id_jobentry, dbe);
		}
	}
	
	public void saveRep(Repository rep, long id_job)
		throws KettleException
	{
		try
		{
			super.saveRep(rep, id_job);
			
			rep.saveJobEntryAttribute(id_job, getID(), "filename", filename);
		}
		catch(KettleDatabaseException dbe)
		{
			throw new KettleException("Unable to save job entry of type 'file exists' to the repository for id_job="+id_job, dbe);
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
            try
            {
                FileObject file = KettleVFS.getFileObject(realFilename);
                if (file.exists() && file.isReadable())
                {
                    log.logDetailed(toString(), "File ["+realFilename+"] exists.");
                    result.setResult( true );
                }
                else
                {
                    log.logDetailed(toString(), "File ["+realFilename+"] doesn't exist!");
                }
            }
            catch (IOException e)
            {
                result.setNrErrors(1);
                log.logError(toString(), "Unexpected error checking filename existance: "+e.toString());
            }
		}
		else
		{
			result.setNrErrors(1);
			log.logError(toString(), "No filename is defined.");
		}
		
		return result;
	}

	public boolean evaluates()
	{
		return true;
	}
    
    public JobEntryDialogInterface getDialog(Shell shell,JobEntryInterface jei,JobMeta jobMeta,String jobName,Repository rep) {
        return new JobEntryFileExistsDialog(shell,this,jobMeta);
    }
}
