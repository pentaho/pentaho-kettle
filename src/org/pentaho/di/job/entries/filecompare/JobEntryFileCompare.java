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

package org.pentaho.di.job.entries.filecompare;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.util.List;

import org.apache.commons.vfs.FileObject;
import org.eclipse.swt.widgets.Shell;
import org.pentaho.di.core.CheckResult;
import org.pentaho.di.core.CheckResultInterface;
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
 * This defines a 'file compare' job entry. It will compare 2 files in a binary way,
 * and will either follow the true flow upon the files being the same or the false
 * flow otherwise.
 *
 * @author Sven Boden
 * @since 01-02-2007
 *
 */
public class JobEntryFileCompare extends JobEntryBase implements Cloneable, JobEntryInterface
{
	private String filename1;
	private String filename2;

	public JobEntryFileCompare(String n)
	{
		super(n, ""); //$NON-NLS-1$
     	filename1=null;
     	filename2=null;
		setID(-1L);
		setJobEntryType(JobEntryType.FILE_COMPARE);
	}

	public JobEntryFileCompare()
	{
		this(""); //$NON-NLS-1$
	}

	public JobEntryFileCompare(JobEntryBase jeb)
	{
		super(jeb);
	}

    public Object clone()
    {
        JobEntryFileCompare je = (JobEntryFileCompare)super.clone();
        return je;
    }

	public String getXML()
	{
        StringBuffer retval = new StringBuffer(50);

		retval.append(super.getXML());
		retval.append("      ").append(XMLHandler.addTagValue("filename1", filename1)); //$NON-NLS-1$ //$NON-NLS-2$
		retval.append("      ").append(XMLHandler.addTagValue("filename2", filename2)); //$NON-NLS-1$ //$NON-NLS-2$

		return retval.toString();
	}

	public void loadXML(Node entrynode, List<DatabaseMeta> databases, Repository rep)
		throws KettleXMLException
	{
		try
		{
			super.loadXML(entrynode, databases);
			filename1 = XMLHandler.getTagValue(entrynode, "filename1"); //$NON-NLS-1$
			filename2 = XMLHandler.getTagValue(entrynode, "filename2"); //$NON-NLS-1$
		}
		catch(KettleXMLException xe)
		{
			throw new KettleXMLException(Messages.getString("JobEntryFileCompare.ERROR_0001_Unable_To_Load_From_Xml_Node"), xe); //$NON-NLS-1$
		}
	}

	public void loadRep(Repository rep, long id_jobentry, List<DatabaseMeta> databases)
		throws KettleException
	{
		try
		{
			super.loadRep(rep, id_jobentry, databases);
			filename1 = rep.getJobEntryAttributeString(id_jobentry, "filename1"); //$NON-NLS-1$
			filename2 = rep.getJobEntryAttributeString(id_jobentry, "filename2"); //$NON-NLS-1$
		}
		catch(KettleException dbe)
		{
			throw new KettleException(Messages.getString("JobEntryFileCompare.ERROR_0002_Unable_To_Load_Job_From_Repository", Long.toString(id_jobentry)), dbe); //$NON-NLS-1$
		}
	}

	public void saveRep(Repository rep, long id_job)
		throws KettleException
	{
		try
		{
			super.saveRep(rep, id_job);

			rep.saveJobEntryAttribute(id_job, getID(), "filename1", filename1); //$NON-NLS-1$
			rep.saveJobEntryAttribute(id_job, getID(), "filename2", filename2); //$NON-NLS-1$
		}
		catch(KettleDatabaseException dbe)
		{
			throw new KettleException(Messages.getString("JobEntryFileCompare.ERROR_0003_Unable_To_Save_Job", Long.toString(id_job)), dbe); //$NON-NLS-1$
		}
	}

    public String getRealFilename1()
    {
        return environmentSubstitute(getFilename1());
    }

    public String getRealFilename2()
    {
        return environmentSubstitute(getFilename2());
    }

    /**
     * Check whether 2 files have the same contents.
     *
     * @param file1 first file to compare
     * @param file2 second file to compare
     * @return true if files are equal, false if they are not
     *
     * @throws IOException upon IO problems
     */
    protected boolean equalFileContents(FileObject file1, FileObject file2)
        throws IOException
    {
   	    // Really read the contents and do comparisons
    		
        DataInputStream in1 = new DataInputStream(new BufferedInputStream(
            		                                       KettleVFS.getInputStream(KettleVFS.getFilename(file1))));
        DataInputStream in2 = new DataInputStream(new BufferedInputStream(
            		                                       KettleVFS.getInputStream(KettleVFS.getFilename(file2))));

        char ch1, ch2;
        while ( in1.available() != 0 && in2.available() != 0 )
        {
          	ch1 = (char)in1.readByte();
       		ch2 = (char)in2.readByte();
       		if ( ch1 != ch2 )
       			return false;
        }
        if ( in1.available() != in2.available() )
        {
          	return false;
        }
        else
        {
          	return true;
        }
   	}

	public Result execute(Result previousResult, int nr, Repository rep, Job parentJob)
	{
		LogWriter log = LogWriter.getInstance();
		Result result = previousResult;
		result.setResult( false );

		String realFilename1 = getRealFilename1();
		String realFilename2 = getRealFilename2();

		FileObject file1 = null;
		FileObject file2 = null;
		try 
		{       
			if (filename1!=null && filename2!=null)
			{
				file1 = KettleVFS.getFileObject(realFilename1);
				file2 = KettleVFS.getFileObject(realFilename2);

				if ( file1.exists() && file2.exists() )
				{
					if ( equalFileContents(file1, file2) )
					{
						result.setResult( true );
					}
					else
					{
						result.setResult( false );
					}
				}
				else
				{
					if ( ! file1.exists() )
						log.logError(toString(), Messages.getString("JobEntryFileCompare.ERROR_0004_File1_Does_Not_Exist", realFilename1)); //$NON-NLS-1$
					if ( ! file2.exists() )
						log.logError(toString(), Messages.getString("JobEntryFileCompare.ERROR_0005_File2_Does_Not_Exist", realFilename2)); //$NON-NLS-1$
					result.setResult( false );
					result.setNrErrors(1);
				}
			}
			else
			{
				log.logError(toString(), Messages.getString("JobEntryFileCompare.ERROR_0006_Need_Two_Filenames")); //$NON-NLS-1$
			}
		}
		catch ( Exception e )
		{
			result.setResult( false );
			result.setNrErrors(1);
			log.logError(toString(), Messages.getString("JobEntryFileCompare.ERROR_0007_Comparing_Files", realFilename2, realFilename2, e.getMessage())); //$NON-NLS-1$
		}	
		finally
		{
			try 
			{
			    if ( file1 != null )
			    	file1.close();
			    
			    if ( file2 != null )
			    	file2.close();			    
		    }
			catch ( IOException e ) { }			
		}
		

		return result;
	}

	public boolean evaluates()
	{
		return true;
	}

    public JobEntryDialogInterface getDialog(Shell shell,JobEntryInterface jei,JobMeta jobMeta,String jobName,Repository rep) {
        return new JobEntryFileCompareDialog(shell,this,jobMeta);
    }

	public void setFilename1(String filename)
	{
		this.filename1 = filename;
	}

	public String getFilename1()
	{
		return filename1;
	}

	public void setFilename2(String filename)
	{
		this.filename2 = filename;
	}

	public String getFilename2()
	{
		return filename2;
	}
  
  public void check(List<CheckResultInterface> remarks, JobMeta jobMeta) {
    if (filename1 != null) {
      remarks.add(new CheckResult(CheckResultInterface.TYPE_RESULT_OK, Messages.getString("JobEntryFileCompare.CheckResult_Filename_1_Defined"), this)); //$NON-NLS-1$
    } else {
      remarks.add(new CheckResult(CheckResultInterface.TYPE_RESULT_ERROR, Messages.getString("JobEntryFileCompare.CheckResult_Filename_1_Not_Defined"), this)); //$NON-NLS-1$
    }
    if (filename2 != null) {
      remarks.add(new CheckResult(CheckResultInterface.TYPE_RESULT_OK, Messages.getString("JobEntryFileCompare.CheckResult_Filename_2_Defined"), this)); //$NON-NLS-1$
    } else {
      remarks.add(new CheckResult(CheckResultInterface.TYPE_RESULT_ERROR, Messages.getString("JobEntryFileCompare.CheckResult_Filename_2_Not_Defined"), this)); //$NON-NLS-1$
    }
    //
    // If either one is null, don't bother continuing...
    //
    if ( (filename1 != null) && (filename2 != null) ) {
      FileObject file1 = null;
      FileObject file2 = null;
      String realFilename1 = getRealFilename1();
      String realFilename2 = getRealFilename2();
      try {
        file1 = KettleVFS.getFileObject(realFilename1);
        if (file1.exists()) {
          remarks.add(new CheckResult(CheckResultInterface.TYPE_RESULT_OK, Messages.getString("JobEntryFileCompare.CheckResult_File_Exists", realFilename1), this)); //$NON-NLS-1$ 
        } else {
          remarks.add(new CheckResult(CheckResultInterface.TYPE_RESULT_ERROR, Messages.getString("JobEntryFileCompare.CheckResult_File_Does_Not_Exist", realFilename1), this));         //$NON-NLS-1$
        }
        try {
          // Not sure if close() here is actually required - just being cautious
          file1.close();
        } catch (IOException ignored) {}
      } catch (IOException ex) {
        remarks.add(new CheckResult(CheckResultInterface.TYPE_RESULT_ERROR, Messages.getString("JobEntryFileCompare.ERROR_0008_File_IOError", realFilename1), this)); //$NON-NLS-1$
      }
      try {
        file2 = KettleVFS.getFileObject(realFilename2);
        if (file2.exists()) {
          remarks.add(new CheckResult(CheckResultInterface.TYPE_RESULT_OK, Messages.getString("JobEntryFileCompare.CheckResult_File_Exists", realFilename2), this)); //$NON-NLS-1$
        } else {
          remarks.add(new CheckResult(CheckResultInterface.TYPE_RESULT_ERROR, Messages.getString("JobEntryFileCompare.CheckResult_File_Does_Not_Exist", realFilename2), this));         //$NON-NLS-1$ 
        }
        try {
          // Not sure if close() here is actually required - just being cautious
          file2.close();
        } catch (IOException ignored) {}
      } catch (IOException ex) {
        remarks.add(new CheckResult(CheckResultInterface.TYPE_RESULT_ERROR, Messages.getString("JobEntryFileCompare.ERROR_0008_File_IOError", realFilename2), this)); //$NON-NLS-1$
      }
    }
  }
    
}