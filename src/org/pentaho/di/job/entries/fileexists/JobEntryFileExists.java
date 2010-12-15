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

import java.util.List;
import java.util.Map;

import org.apache.commons.vfs.FileObject;
import org.pentaho.di.cluster.SlaveServer;
import org.pentaho.di.core.CheckResultInterface;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.Result;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.exception.KettleDatabaseException;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleXMLException;
import org.pentaho.di.core.variables.VariableSpace;
import org.pentaho.di.core.vfs.KettleVFS;
import org.pentaho.di.core.xml.XMLHandler;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.job.JobMeta;
import org.pentaho.di.job.entry.JobEntryBase;
import org.pentaho.di.job.entry.JobEntryInterface;
import org.pentaho.di.repository.ObjectId;
import org.pentaho.di.repository.Repository;
import org.pentaho.di.resource.ResourceDefinition;
import org.pentaho.di.resource.ResourceEntry;
import org.pentaho.di.resource.ResourceNamingInterface;
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
	private static Class<?> PKG = JobEntryFileExists.class; // for i18n purposes, needed by Translator2!!   $NON-NLS-1$

	private String filename;

	public JobEntryFileExists(String n)
	{
		super(n, ""); //$NON-NLS-1$
		filename=null;
		setID(-1L);
	}

	public JobEntryFileExists()
	{
		this(""); //$NON-NLS-1$
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
			throw new KettleXMLException(BaseMessages.getString(PKG, "JobEntryFileExists.ERROR_0001_Cannot_Load_Job_Entry_From_Xml_Node"), xe); //$NON-NLS-1$
		}
	}

	public void loadRep(Repository rep, ObjectId id_jobentry, List<DatabaseMeta> databases, List<SlaveServer> slaveServers) throws KettleException
	{
		try
		{
			filename = rep.getJobEntryAttributeString(id_jobentry, "filename"); //$NON-NLS-1$
		}
		catch(KettleException dbe)
		{
			throw new KettleException(BaseMessages.getString(PKG, "JobEntryFileExists.ERROR_0002_Cannot_Load_Job_From_Repository", id_jobentry), dbe); //$NON-NLS-1$
		}
	}

	public void saveRep(Repository rep, ObjectId id_job) throws KettleException
	{
		try
		{
			rep.saveJobEntryAttribute(id_job, getObjectId(), "filename", filename); //$NON-NLS-1$
		}
		catch(KettleDatabaseException dbe)
		{
			throw new KettleException(BaseMessages.getString(PKG, "JobEntryFileExists.ERROR_0003_Cannot_Save_Job_Entry", id_job), dbe); //$NON-NLS-1$
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

	public Result execute(Result previousResult, int nr)
	{
		Result result = previousResult;
		result.setResult( false );

		if (filename!=null)
		{
            String realFilename = getRealFilename();
            try
            {
                FileObject file = KettleVFS.getFileObject(realFilename, this);
                if (file.exists() && file.isReadable())
                {
                    logDetailed(BaseMessages.getString(PKG, "JobEntryFileExists.File_Exists", realFilename)); //$NON-NLS-1$
                    result.setResult( true );
                }
                else
                {
                    logDetailed(BaseMessages.getString(PKG, "JobEntryFileExists.File_Does_Not_Exist", realFilename)); //$NON-NLS-1$
                }
            }
            catch (Exception e)
            {
                result.setNrErrors(1);
                logError(BaseMessages.getString(PKG, "JobEntryFileExists.ERROR_0004_IO_Exception", e.getMessage()), e); //$NON-NLS-1$
            }
		}
		else
		{
			result.setNrErrors(1);
			logError(BaseMessages.getString(PKG, "JobEntryFileExists.ERROR_0005_No_Filename_Defined")); //$NON-NLS-1$
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

	/**
	 * Since the exported job that runs this will reside in a ZIP file, we can't reference files relatively.
	 * So what this does is turn the name of files into absolute paths OR it simply includes the resource in the ZIP file.
	 * For now, we'll simply turn it into an absolute path and pray that the file is on a shared drive or something like that.
	 * TODO: create options to configure this behavior 
	 */
	public String exportResources(VariableSpace space, Map<String, ResourceDefinition> definitions, ResourceNamingInterface resourceNamingInterface, Repository repository) throws KettleException {
		try {
			// The object that we're modifying here is a copy of the original!
			// So let's change the filename from relative to absolute by grabbing the file object...
			// In case the name of the file comes from previous steps, forget about this!
			//
			if (!Const.isEmpty(filename)) {
				// From : ${FOLDER}/../foo/bar.csv
				// To   : /home/matt/test/files/foo/bar.csv
				//
				FileObject fileObject = KettleVFS.getFileObject(space.environmentSubstitute(filename), space);
				
				// If the file doesn't exist, forget about this effort too!
				//
				if (fileObject.exists()) {
					// Convert to an absolute path...
					// 
					filename = resourceNamingInterface.nameResource(fileObject, space, true);
					
					return filename;
				}
			}
			return null;
		} catch (Exception e) {
			throw new KettleException(e); //$NON-NLS-1$
		}
	}
}
