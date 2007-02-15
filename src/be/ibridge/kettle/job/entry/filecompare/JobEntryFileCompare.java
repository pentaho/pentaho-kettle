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

package be.ibridge.kettle.job.entry.filecompare;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;

import org.apache.commons.vfs.FileObject;
import org.eclipse.swt.widgets.Shell;
import org.w3c.dom.Node;

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
		super(n, "");
     	filename1=null;
     	filename2=null;
		setID(-1L);
		setType(JobEntryInterface.TYPE_JOBENTRY_FILE_COMPARE);
	}

	public JobEntryFileCompare()
	{
		this("");
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
		retval.append("      ").append(XMLHandler.addTagValue("filename1", filename1));
		retval.append("      ").append(XMLHandler.addTagValue("filename2", filename2));

		return retval.toString();
	}

	public void loadXML(Node entrynode, ArrayList databases, Repository rep)
		throws KettleXMLException
	{
		try
		{
			super.loadXML(entrynode, databases);
			filename1 = XMLHandler.getTagValue(entrynode, "filename1");
			filename2 = XMLHandler.getTagValue(entrynode, "filename2");
		}
		catch(KettleXMLException xe)
		{
			throw new KettleXMLException("Unable to load job entry of type 'file compare' from XML node", xe);
		}
	}

	public void loadRep(Repository rep, long id_jobentry, ArrayList databases)
		throws KettleException
	{
		try
		{
			super.loadRep(rep, id_jobentry, databases);
			filename1 = rep.getJobEntryAttributeString(id_jobentry, "filename1");
			filename2 = rep.getJobEntryAttributeString(id_jobentry, "filename2");
		}
		catch(KettleException dbe)
		{
			throw new KettleException("Unable to load job entry of type 'file compare' from the repository for id_jobentry="+id_jobentry, dbe);
		}
	}

	public void saveRep(Repository rep, long id_job)
		throws KettleException
	{
		try
		{
			super.saveRep(rep, id_job);

			rep.saveJobEntryAttribute(id_job, getID(), "filename1", filename1);
			rep.saveJobEntryAttribute(id_job, getID(), "filename2", filename2);
		}
		catch(KettleDatabaseException dbe)
		{
			throw new KettleException("Unable to save job entry of type 'file compare' to the repository for id_job="+id_job, dbe);
		}
	}

    public String getRealFilename1()
    {
        return StringUtil.environmentSubstitute(getFilename1());
    }

    public String getRealFilename2()
    {
        return StringUtil.environmentSubstitute(getFilename2());
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

	public Result execute(Result prev_result, int nr, Repository rep, Job parentJob)
	{
		LogWriter log = LogWriter.getInstance();
		Result result = new Result(nr);
		result.setResult( false );

		String realFilename1 = getRealFilename1();
		String realFilename2 = getRealFilename2();

		try 
		{       
			if (filename1!=null && filename2!=null)
			{
				FileObject file1 = KettleVFS.getFileObject(realFilename1);
				FileObject file2 = KettleVFS.getFileObject(realFilename2);

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
						log.logError(toString(), "File 1 [" + realFilename1 + "] does not exist.");
					if ( ! file2.exists() )
						log.logError(toString(), "File 2 [" + realFilename2 + "] does not exist.");
					result.setResult( false );
					result.setNrErrors(1);
				}
			}
			else
			{
				log.logError(toString(), "Need 2 filenames to compare file contents.");
			}

		}
		catch ( Exception e )
		{
			result.setResult( false );
			result.setNrErrors(1);
			log.logError(toString(), "Error occurred while comparing file [" + realFilename2 + 
					"] and file [" + realFilename2 + "]: " + e.getMessage());
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
}