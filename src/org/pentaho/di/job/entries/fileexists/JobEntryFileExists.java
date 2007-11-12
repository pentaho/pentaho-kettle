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

package org.pentaho.di.job.entries.fileexists;

import static org.pentaho.di.job.entry.validator.AndValidator.putValidators;
import static org.pentaho.di.job.entry.validator.JobEntryValidatorUtils.andValidator;
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

	public void loadXML(Node entrynode, List<DatabaseMeta> databases, List<SlaveServer> slaveServers, Repository rep) throws KettleXMLException
	{
		try
		{
			super.loadXML(entrynode, databases, slaveServers);
			filename      = XMLHandler.getTagValue(entrynode, "filename"); //$NON-NLS-1$
		}
		catch(KettleXMLException xe)
		{
			throw new KettleXMLException(Messages.getString("JobEntryFileExists.ERROR_0001_Cannot_Load_Job_Entry_From_Xml_Node"), xe); //$NON-NLS-1$
		}
	}

	public void loadRep(Repository rep, long id_jobentry, List<DatabaseMeta> databases, List<SlaveServer> slaveServers) throws KettleException
	{
		try
		{
			super.loadRep(rep, id_jobentry, databases, slaveServers);
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
  public void check(List<CheckResultInterface> remarks, JobMeta jobMeta) {
    andValidator().validate(this, "filename", remarks, putValidators(notBlankValidator())); //$NON-NLS-1$
  }


}
