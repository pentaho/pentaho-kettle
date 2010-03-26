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

package org.pentaho.di.job.entries.filecompare;

import static org.pentaho.di.job.entry.validator.AbstractFileValidator.putVariableSpace;
import static org.pentaho.di.job.entry.validator.AndValidator.putValidators;
import static org.pentaho.di.job.entry.validator.JobEntryValidatorUtils.andValidator;
import static org.pentaho.di.job.entry.validator.JobEntryValidatorUtils.fileExistsValidator;
import static org.pentaho.di.job.entry.validator.JobEntryValidatorUtils.notNullValidator;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.util.List;

import org.apache.commons.vfs.FileObject;
import org.apache.commons.vfs.FileType;
import org.pentaho.di.cluster.SlaveServer;
import org.pentaho.di.core.CheckResultInterface;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.Result;
import org.pentaho.di.core.ResultFile;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.exception.KettleDatabaseException;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleFileException;
import org.pentaho.di.core.exception.KettleXMLException;
import org.pentaho.di.core.vfs.KettleVFS;
import org.pentaho.di.core.xml.XMLHandler;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.job.JobMeta;
import org.pentaho.di.job.entry.JobEntryBase;
import org.pentaho.di.job.entry.JobEntryInterface;
import org.pentaho.di.job.entry.validator.ValidatorContext;
import org.pentaho.di.repository.ObjectId;
import org.pentaho.di.repository.Repository;
import org.pentaho.di.resource.ResourceEntry;
import org.pentaho.di.resource.ResourceReference;
import org.pentaho.di.resource.ResourceEntry.ResourceType;
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
	private static Class<?> PKG = JobEntryFileCompare.class; // for i18n purposes, needed by Translator2!!   $NON-NLS-1$

	private String filename1;
	private String filename2;
	private boolean addFilenameToResult;

	public JobEntryFileCompare(String n)
	{
		super(n, ""); //$NON-NLS-1$
     	filename1 = null;
     	filename2=null;
     	addFilenameToResult=false;
		setID(-1L);
	}

	public JobEntryFileCompare()
	{
		this(""); //$NON-NLS-1$
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
		retval.append("      ").append(XMLHandler.addTagValue("add_filename_result", addFilenameToResult));
		return retval.toString();
	}

	public void loadXML(Node entrynode, List<DatabaseMeta> databases, List<SlaveServer> slaveServers, Repository rep)
		throws KettleXMLException
	{
		try
		{
			super.loadXML(entrynode, databases, slaveServers);
			filename1 = XMLHandler.getTagValue(entrynode, "filename1"); //$NON-NLS-1$
			filename2 = XMLHandler.getTagValue(entrynode, "filename2"); //$NON-NLS-1$
			addFilenameToResult = "Y".equalsIgnoreCase(XMLHandler.getTagValue(entrynode, "add_filename_result"));
		}
		catch(KettleXMLException xe)
		{
			throw new KettleXMLException(BaseMessages.getString(PKG, "JobEntryFileCompare.ERROR_0001_Unable_To_Load_From_Xml_Node"), xe); //$NON-NLS-1$
		}
	}

	public void loadRep(Repository rep, ObjectId id_jobentry, List<DatabaseMeta> databases, List<SlaveServer> slaveServers) throws KettleException
	{
		try
		{
			filename1 = rep.getJobEntryAttributeString(id_jobentry, "filename1"); //$NON-NLS-1$
			filename2 = rep.getJobEntryAttributeString(id_jobentry, "filename2"); //$NON-NLS-1$
			addFilenameToResult = rep.getJobEntryAttributeBoolean(id_jobentry, "add_filename_result");
		}
		catch(KettleException dbe)
		{
			throw new KettleException(BaseMessages.getString(PKG, "JobEntryFileCompare.ERROR_0002_Unable_To_Load_Job_From_Repository", id_jobentry), dbe); //$NON-NLS-1$
		}
	}

	public void saveRep(Repository rep, ObjectId id_job) throws KettleException
	{
		try
		{
			rep.saveJobEntryAttribute(id_job, getObjectId(), "filename1", filename1); //$NON-NLS-1$
			rep.saveJobEntryAttribute(id_job, getObjectId(), "filename2", filename2); //$NON-NLS-1$
            rep.saveJobEntryAttribute(id_job, getObjectId(), "add_filename_result", addFilenameToResult);
		}
		catch(KettleDatabaseException dbe)
		{
			throw new KettleException(BaseMessages.getString(PKG, "JobEntryFileCompare.ERROR_0003_Unable_To_Save_Job", id_job), dbe); //$NON-NLS-1$
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
        throws KettleFileException
    {
   	    // Really read the contents and do comparisons

    	try {
	        DataInputStream in1 = new DataInputStream(new BufferedInputStream(KettleVFS.getInputStream(KettleVFS.getFilename(file1), this)));
	        DataInputStream in2 = new DataInputStream(new BufferedInputStream(KettleVFS.getInputStream(KettleVFS.getFilename(file2), this)));
	
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
    	} catch(IOException e) {
    		throw new KettleFileException(e);
    	}
   	}

	public Result execute(Result previousResult, int nr)
	{
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
				file1 = KettleVFS.getFileObject(realFilename1, this);
				file2 = KettleVFS.getFileObject(realFilename2, this);

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
					
					//add filename to result filenames
					if(addFilenameToResult && file1.getType()==FileType.FILE && file2.getType()==FileType.FILE)
					{
						ResultFile resultFile = new ResultFile(ResultFile.FILE_TYPE_GENERAL , file1, parentJob.getJobname(), toString());
						resultFile.setComment(BaseMessages.getString(PKG, "JobWaitForFile.FilenameAdded"));
						result.getResultFiles().put(resultFile.getFile().toString(), resultFile);
						resultFile = new ResultFile(ResultFile.FILE_TYPE_GENERAL , file2, parentJob.getJobname(), toString());
						resultFile.setComment(BaseMessages.getString(PKG, "JobWaitForFile.FilenameAdded"));
						result.getResultFiles().put(resultFile.getFile().toString(), resultFile);
					 }
				}
				else
				{
					if ( ! file1.exists() )
						logError(BaseMessages.getString(PKG, "JobEntryFileCompare.ERROR_0004_File1_Does_Not_Exist", realFilename1)); //$NON-NLS-1$
					if ( ! file2.exists() )
						logError(BaseMessages.getString(PKG, "JobEntryFileCompare.ERROR_0005_File2_Does_Not_Exist", realFilename2)); //$NON-NLS-1$
					result.setResult( false );
					result.setNrErrors(1);
				}
			}
			else
			{
				logError(BaseMessages.getString(PKG, "JobEntryFileCompare.ERROR_0006_Need_Two_Filenames")); //$NON-NLS-1$
			}
		}
		catch ( Exception e )
		{
			result.setResult( false );
			result.setNrErrors(1);
			logError(BaseMessages.getString(PKG, "JobEntryFileCompare.ERROR_0007_Comparing_Files", realFilename2, realFilename2, e.getMessage())); //$NON-NLS-1$
		}
		finally
		{
			try
			{
			    if ( file1 != null ) {
			    	file1.close();
			    	file1=null;
			    }

			    if ( file2 != null ) {
			    	file2.close();
			    	file2=null;
			    }
		    }
			catch ( IOException e ) { }
		}


		return result;
	}

	public boolean evaluates()
	{
		return true;
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
	public boolean isAddFilenameToResult() {
		return addFilenameToResult;
	}
	
	public void setAddFilenameToResult(boolean addFilenameToResult) {
		this.addFilenameToResult = addFilenameToResult;
	}
  public List<ResourceReference> getResourceDependencies(JobMeta jobMeta) {
    List<ResourceReference> references = super.getResourceDependencies(jobMeta);
    if ((!Const.isEmpty(filename1)) && (!Const.isEmpty(filename2)) ) {
      String realFilename1 = jobMeta.environmentSubstitute(filename1);
      String realFilename2 = jobMeta.environmentSubstitute(filename2);
      ResourceReference reference = new ResourceReference(this);
      reference.getEntries().add( new ResourceEntry(realFilename1, ResourceType.FILE));
      reference.getEntries().add( new ResourceEntry(realFilename2, ResourceType.FILE));
      references.add(reference);
    }
    return references;
  }

  public void check(List<CheckResultInterface> remarks, JobMeta jobMeta) {
    ValidatorContext ctx = new ValidatorContext();
    putVariableSpace(ctx, getVariables());
    putValidators(ctx, notNullValidator(), fileExistsValidator());
    andValidator().validate(this, "filename1", remarks, ctx); //$NON-NLS-1$
    andValidator().validate(this, "filename2", remarks, ctx); //$NON-NLS-1$
  }

}