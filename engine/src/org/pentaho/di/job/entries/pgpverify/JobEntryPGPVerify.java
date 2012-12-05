/*******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2012 by Pentaho : http://www.pentaho.com
 *
 *******************************************************************************
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 ******************************************************************************/

package org.pentaho.di.job.entries.pgpverify;

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
import org.pentaho.di.job.entries.pgpencryptfiles.GPG;
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
 * This defines a PGP verify job entry.
 *
 * @author Samatar
 * @since 25-02-2011
 *
 */

public class JobEntryPGPVerify extends JobEntryBase implements Cloneable, JobEntryInterface
{
	private static Class<?> PKG = JobEntryPGPVerify.class; // for i18n purposes, needed by Translator2!!   $NON-NLS-1$

	private String gpglocation;
	private String filename;
	private String detachedfilename;
	private boolean useDetachedSignature;

	public JobEntryPGPVerify(String n)
	{
		super(n, ""); //$NON-NLS-1$
		gpglocation=null;
		filename=null;
		detachedfilename=null;
		useDetachedSignature=false;
		setID(-1L);
	}

	public JobEntryPGPVerify()
	{
		this(""); //$NON-NLS-1$
	}

    public Object clone()
    {
        JobEntryPGPVerify je = (JobEntryPGPVerify) super.clone();
        return je;
    }

	public String getXML()
	{
        StringBuffer retval = new StringBuffer();

		retval.append(super.getXML());
		retval.append("      ").append(XMLHandler.addTagValue("gpglocation",   gpglocation)); //$NON-NLS-1$ //$NON-NLS-2$
		retval.append("      ").append(XMLHandler.addTagValue("filename",   filename));
		retval.append("      ").append(XMLHandler.addTagValue("detachedfilename",   detachedfilename));
		retval.append("      ").append(XMLHandler.addTagValue("useDetachedSignature",   useDetachedSignature));
		return retval.toString();
	}

	public void loadXML(Node entrynode, List<DatabaseMeta> databases, List<SlaveServer> slaveServers, Repository rep) throws KettleXMLException
	{
		try
		{
			super.loadXML(entrynode, databases, slaveServers);
			gpglocation      = XMLHandler.getTagValue(entrynode, "gpglocation"); //$NON-NLS-1$
			filename      = XMLHandler.getTagValue(entrynode, "filename");
			detachedfilename      = XMLHandler.getTagValue(entrynode, "detachedfilename");
			useDetachedSignature = "Y".equalsIgnoreCase(XMLHandler.getTagValue(entrynode, "useDetachedSignature")); //$NON-NLS-1$ //$NON-NLS-2$

		}
		catch(KettleXMLException xe)
		{
			throw new KettleXMLException(BaseMessages.getString(PKG, "JobEntryPGPVerify.ERROR_0001_Cannot_Load_Job_Entry_From_Xml_Node"), xe); //$NON-NLS-1$
		}
	}

	public void loadRep(Repository rep, ObjectId id_jobentry, List<DatabaseMeta> databases, List<SlaveServer> slaveServers) throws KettleException
	{
		try
		{
			gpglocation = rep.getJobEntryAttributeString(id_jobentry, "gpglocation"); //$NON-NLS-1$
			filename = rep.getJobEntryAttributeString(id_jobentry, "filename");
			detachedfilename = rep.getJobEntryAttributeString(id_jobentry, "detachedfilename");
			useDetachedSignature = rep.getJobEntryAttributeBoolean(id_jobentry, "useDetachedSignature");
		}
		catch(KettleException dbe)
		{
			throw new KettleException(BaseMessages.getString(PKG, "JobEntryPGPVerify.ERROR_0002_Cannot_Load_Job_From_Repository", id_jobentry), dbe); //$NON-NLS-1$
		}
	}

	public void saveRep(Repository rep, ObjectId id_job) throws KettleException
	{
		try
		{
			rep.saveJobEntryAttribute(id_job, getObjectId(), "gpglocation", gpglocation); //$NON-NLS-1$
			rep.saveJobEntryAttribute(id_job, getObjectId(), "filename", filename);
			rep.saveJobEntryAttribute(id_job, getObjectId(), "detachedfilename", detachedfilename);
			rep.saveJobEntryAttribute(id_job, getObjectId(), "useDetachedSignature", useDetachedSignature);
		}
		catch(KettleDatabaseException dbe)
		{
			throw new KettleException(BaseMessages.getString(PKG, "JobEntryPGPVerify.ERROR_0003_Cannot_Save_Job_Entry", id_job), dbe); //$NON-NLS-1$
		}
	}

	public void setGPGLocation(String gpglocation)
	{
		this.gpglocation = gpglocation;
	}

	public String getGPGLocation()
	{
		return gpglocation;
	}
	public void setFilename(String filename)
	{
		this.filename = filename;
	}

	public String getFilename()
	{
		return filename;
	}
	public void setDetachedfilename(String detachedfilename)
	{
		this.detachedfilename = detachedfilename;
	}

	public String getDetachedfilename()
	{
		return detachedfilename;
	}
	public void setUseDetachedfilename(boolean useDetachedSignature)
	{
		this.useDetachedSignature = useDetachedSignature;
	}

	public boolean useDetachedfilename()
	{
		return useDetachedSignature;
	}
	public Result execute(Result previousResult, int nr)
	{
		Result result = previousResult;
		result.setResult( false );
		result.setNrErrors(1);
		
		FileObject file=null;
		FileObject detachedSignature=null;
		try {
			
			String realFilename = environmentSubstitute(getFilename());
			if(Const.isEmpty(realFilename)) {
				logError(BaseMessages.getString(PKG, "JobPGPVerify.FilenameMissing"));
				return result;
			}
			file= KettleVFS.getFileObject(realFilename);
			
			GPG gpg = new GPG(environmentSubstitute(getGPGLocation()), log);
			
			if(useDetachedfilename()) {
				String signature = environmentSubstitute(getDetachedfilename());
				
				if(Const.isEmpty(signature)) {
					logError(BaseMessages.getString(PKG, "JobPGPVerify.DetachedSignatureMissing"));
					return result;
				}
				detachedSignature= KettleVFS.getFileObject(signature);
				
				gpg.verifyDetachedSignature(detachedSignature, file);
			}else {
				gpg.verifySignature(file);
			}
			
			result.setNrErrors(0);
			result.setResult(true);
			
		}catch(Exception e) {
			logError(BaseMessages.getString(PKG, "JobPGPVerify.Error"), e);
		}finally {
			try {
				if(file!=null) file.close();
				if(detachedSignature!=null) detachedSignature.close();
			}catch(Exception e){};
		}

		return result;
	}

	public boolean evaluates()
	{
		return true;
	}

  public List<ResourceReference> getResourceDependencies(JobMeta jobMeta) {
    List<ResourceReference> references = super.getResourceDependencies(jobMeta);
    if (!Const.isEmpty(gpglocation)) {
      String realFileName = jobMeta.environmentSubstitute(gpglocation);
      ResourceReference reference = new ResourceReference(this);
      reference.getEntries().add( new ResourceEntry(realFileName, ResourceType.FILE));
      references.add(reference);
    }
    return references;
  }

  @Override
  public void check(List<CheckResultInterface> remarks, JobMeta jobMeta) {
    andValidator().validate(this, "gpglocation", remarks, putValidators(notBlankValidator())); //$NON-NLS-1$
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
			// So let's change the gpglocation from relative to absolute by grabbing the file object...
			// In case the name of the file comes from previous steps, forget about this!
			//
			if (!Const.isEmpty(gpglocation)) {
				// From : ${FOLDER}/../foo/bar.csv
				// To   : /home/matt/test/files/foo/bar.csv
				//
				FileObject fileObject = KettleVFS.getFileObject(space.environmentSubstitute(gpglocation), space);
				
				// If the file doesn't exist, forget about this effort too!
				//
				if (fileObject.exists()) {
					// Convert to an absolute path...
					// 
					gpglocation = resourceNamingInterface.nameResource(fileObject, space, true);
					
					return gpglocation;
				}
			}
			return null;
		} catch (Exception e) {
			throw new KettleException(e); //$NON-NLS-1$
		}
	}
}
