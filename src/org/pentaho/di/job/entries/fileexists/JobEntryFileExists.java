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
import java.util.List;

import org.apache.commons.vfs.FileObject;
import org.eclipse.swt.widgets.Shell;
import org.pentaho.di.core.CheckResult;
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
import org.pentaho.di.job.entry.JobEntryDialogInterface;
import org.pentaho.di.job.entry.JobEntryInterface;
import org.pentaho.di.repository.Repository;
import org.w3c.dom.Node;




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
		super(n, ""); //$NON-NLS-1$
		filename=null;
		setID(-1L);
		setJobEntryType(JobEntryType.FILE_EXISTS);
	}

	public JobEntryFileExists()
	{
		this(""); //$NON-NLS-1$
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
		retval.append("      ").append(XMLHandler.addTagValue("filename",   filename)); //$NON-NLS-1$ //$NON-NLS-2$
		
		return retval.toString();
	}
	
	public void loadXML(Node entrynode, List<DatabaseMeta> databases, Repository rep)
		throws KettleXMLException
	{
		try
		{
			super.loadXML(entrynode, databases);
			filename      = XMLHandler.getTagValue(entrynode, "filename"); //$NON-NLS-1$
		}
		catch(KettleXMLException xe)
		{
			throw new KettleXMLException(Messages.getString("JobEntryFileExists.ERROR_0001_Cannot_Load_Job_Entry_From_Xml_Node"), xe); //$NON-NLS-1$
		}
	}

	public void loadRep(Repository rep, long id_jobentry, List<DatabaseMeta> databases)
		throws KettleException
	{
		try
		{
			super.loadRep(rep, id_jobentry, databases);
			filename = rep.getJobEntryAttributeString(id_jobentry, "filename"); //$NON-NLS-1$
		}
		catch(KettleException dbe)
		{
			throw new KettleException(Messages.getString("JobEntryFileExists.ERROR_0002_Cannot_Load_Job_From_Repository", Long.toString(id_jobentry)), dbe); //$NON-NLS-1$
		}
	}
	
	public void saveRep(Repository rep, long id_job)
		throws KettleException
	{
		try
		{
			super.saveRep(rep, id_job);
			
			rep.saveJobEntryAttribute(id_job, getID(), "filename", filename); //$NON-NLS-1$
		}
		catch(KettleDatabaseException dbe)
		{
			throw new KettleException(Messages.getString("JobEntryFileExists.ERROR_0003_Cannot_Save_Job_Entry", Long.toString(id_job)), dbe); //$NON-NLS-1$
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
	
		if (filename!=null)
		{
            String realFilename = getRealFilename();
            try
            {
                FileObject file = KettleVFS.getFileObject(realFilename);
                if (file.exists() && file.isReadable())
                {
                    log.logDetailed(toString(), Messages.getString("JobEntryFileExists.File_Exists", realFilename)); //$NON-NLS-1$
                    result.setResult( true );
                }
                else
                {
                    log.logDetailed(toString(), Messages.getString("JobEntryFileExists.File_Does_Not_Exist", realFilename)); //$NON-NLS-1$
                }
            }
            catch (IOException e)
            {
                result.setNrErrors(1);
                log.logError(toString(), Messages.getString("JobEntryFileExists.ERROR_0004_IO_Exception", e.toString())); //$NON-NLS-1$
            }
		}
		else
		{
			result.setNrErrors(1);
			log.logError(toString(), Messages.getString("JobEntryFileExists.ERROR_0005_No_Filename_Defined")); //$NON-NLS-1$
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
    
    public void check(List<CheckResult> remarks, JobMeta jobMeta) {
      if (filename != null) {
        remarks.add(new CheckResult(CheckResult.TYPE_RESULT_OK, Messages.getString("JobEntryFileExists.CheckResult.Filename_Is_Defined"), this)); //$NON-NLS-1$
        String realFilename = getRealFilename();
        try {
          FileObject file = KettleVFS.getFileObject(realFilename);
          if (file.exists() && file.isReadable()) {
            remarks.add(new CheckResult(CheckResult.TYPE_RESULT_OK, Messages.getString("JobEntryFileExists.CheckResult.File_Exists", realFilename), this)); //$NON-NLS-1$
          } else {
            remarks.add(new CheckResult(CheckResult.TYPE_RESULT_WARNING, Messages.getString("JobEntryFileExists.CheckResult.File_Does_Not_Exist", realFilename), this)); //$NON-NLS-1$
          }
          try {
            file.close(); // Paranoia
          } catch (IOException ignored) {}
        } catch (IOException ex) {
          remarks.add(new CheckResult(CheckResult.TYPE_RESULT_ERROR, Messages.getString("JobEntryFileExists.CheckResult.File_Received_IO_Error", filename), this)); //$NON-NLS-1$
        }
      } else {
        remarks.add(new CheckResult(CheckResult.TYPE_RESULT_ERROR, Messages.getString("JobEntryFileExists.CheckResult.File_Name_Not_Defined"), this)); //$NON-NLS-1$
      }
    }
      
    
}
