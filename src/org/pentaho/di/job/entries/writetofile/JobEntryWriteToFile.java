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

package org.pentaho.di.job.entries.writetofile;

import static org.pentaho.di.job.entry.validator.AndValidator.putValidators;
import static org.pentaho.di.job.entry.validator.JobEntryValidatorUtils.andValidator;
import static org.pentaho.di.job.entry.validator.JobEntryValidatorUtils.notBlankValidator;


import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.util.List;

import org.apache.commons.vfs.FileObject;
import org.w3c.dom.Node;

import org.pentaho.di.cluster.SlaveServer;
import org.pentaho.di.core.CheckResultInterface;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.Result;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.exception.KettleDatabaseException;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleXMLException;
import org.pentaho.di.core.vfs.KettleVFS;
import org.pentaho.di.core.xml.XMLHandler;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.job.JobMeta;
import org.pentaho.di.job.entry.JobEntryBase;
import org.pentaho.di.job.entry.JobEntryInterface;
import org.pentaho.di.repository.ObjectId;
import org.pentaho.di.repository.Repository;
import org.pentaho.di.resource.ResourceEntry;
import org.pentaho.di.resource.ResourceReference;
import org.pentaho.di.resource.ResourceEntry.ResourceType;



/**
 * This defines a 'write to file' job entry. Its main use would be to create empty
 * trigger files that can be used to control the flow in ETL cycles.
 * 
 * @author Samatar Hassan
 * @since 28-01-2007
 *
 */
public class JobEntryWriteToFile extends JobEntryBase implements Cloneable, JobEntryInterface
{
    private static Class<?> PKG = JobEntryWriteToFile.class; // for i18n purposes, needed by Translator2!!   $NON-NLS-1$

	private String filename;
	private boolean createParentFolder;
	private boolean appendFile;
	private String content;
	private String encoding;
	
	public JobEntryWriteToFile(String n)
	{
		super(n, "");
		filename=null;
		createParentFolder=false;
		appendFile=false;
		content=null;
		encoding=null;
		setID(-1L);
	}

	public JobEntryWriteToFile()
	{
		this("");
	}

    public Object clone()
    {
    	JobEntryWriteToFile je = (JobEntryWriteToFile) super.clone();
        return je;
    }
	public String getXML()
	{
        StringBuffer retval = new StringBuffer(50);
		
		retval.append(super.getXML());		
		retval.append("      ").append(XMLHandler.addTagValue("filename",   filename));
		retval.append("      ").append(XMLHandler.addTagValue("createParentFolder", createParentFolder));
		retval.append("      ").append(XMLHandler.addTagValue("appendFile", appendFile));
		retval.append("      ").append(XMLHandler.addTagValue("content", content));
		retval.append("      ").append(XMLHandler.addTagValue("encoding", encoding));
		return retval.toString();
	}
	
	public void loadXML(Node entrynode, List<DatabaseMeta> databases, List<SlaveServer> slaveServers, Repository rep)
	throws KettleXMLException
	{
		try
		{
			super.loadXML(entrynode, databases, slaveServers);
			filename = XMLHandler.getTagValue(entrynode, "filename");
			createParentFolder = "Y".equalsIgnoreCase(XMLHandler.getTagValue(entrynode, "createParentFolder"));
			appendFile = "Y".equalsIgnoreCase(XMLHandler.getTagValue(entrynode, "appendFile"));
			content = XMLHandler.getTagValue(entrynode, "content");
			encoding = XMLHandler.getTagValue(entrynode, "encoding");
		}
		catch(KettleXMLException xe)
		{
			throw new KettleXMLException("Unable to load job entry of type 'create file' from XML node", xe);
		}
	}

	public void loadRep(Repository rep, ObjectId id_jobentry, List<DatabaseMeta> databases, List<SlaveServer> slaveServers) throws KettleException
    {
		try
		{
			filename = rep.getJobEntryAttributeString(id_jobentry, "filename");
			createParentFolder = rep.getJobEntryAttributeBoolean(id_jobentry, "createParentFolder");
			appendFile = rep.getJobEntryAttributeBoolean(id_jobentry, "appendFile");
			content = rep.getJobEntryAttributeString(id_jobentry, "content");
			encoding = rep.getJobEntryAttributeString(id_jobentry, "encoding");
		}
		catch(KettleException dbe)
		{
			throw new KettleException("Unable to load job entry of type 'create file' from the repository for id_jobentry="+id_jobentry, dbe);
		}
	}
	
	public void saveRep(Repository rep, ObjectId id_job)
	throws KettleException
	{
		try
		{
			rep.saveJobEntryAttribute(id_job, getObjectId(), "filename", filename);
            rep.saveJobEntryAttribute(id_job, getObjectId(), "createParentFolder", createParentFolder);
            rep.saveJobEntryAttribute(id_job, getObjectId(), "appendFile", appendFile);
        	rep.saveJobEntryAttribute(id_job, getObjectId(), "content", content);
        	rep.saveJobEntryAttribute(id_job, getObjectId(), "encoding", encoding);
		}
		catch(KettleDatabaseException dbe)
		{
			throw new KettleException("Unable to save job entry of type 'create file' to the repository for id_job="+id_job, dbe);
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
	public void setContent(String content)
	{
		this.content = content;
	}
	
	public String getContent()
	{
		return content;
	}
	public void setEncoding(String encoding)
	{
		this.encoding = encoding;
	}
	
	public String getEncoding()
	{
		return encoding;
	}
    public String getRealFilename()
    {
        return environmentSubstitute(getFilename());
    }
	
	public Result execute(Result previousResult, int nr)
	{
		Result result = previousResult;
		result.setResult( false );
		result.setNrErrors(1);	
		
        String realFilename = getRealFilename();
		if (!Const.isEmpty(realFilename)) {
			String content = environmentSubstitute(getContent());
			String encoding = environmentSubstitute(getEncoding());
			
            OutputStreamWriter osw=null;
            FileOutputStream  fos=null;
			try {
				
				// Create parent folder if needed
				createParentFolder(realFilename);
				
				// if uploaded file not exist, create a new one
				fos = new FileOutputStream (realFilename, isAppendFile());
				if(Const.isEmpty(encoding)) {
					if(isDebug()) logDebug(BaseMessages.getString(PKG, "JobWriteToFile.Log.WritingToFile", realFilename));
					osw = new OutputStreamWriter(fos);
				}else {
					if(isDebug()) logDebug(BaseMessages.getString(PKG, "JobWriteToFile.Log.WritingToFileWithEncoding", realFilename, encoding));
					osw = new OutputStreamWriter(fos, encoding);
				}
				osw.write(content); 
	
				result.setResult( true );
				result.setNrErrors(0);	
			    
			} catch (Exception e) {
				logError(BaseMessages.getString(PKG, "JobWriteToFile.Error.WritingFile", realFilename, e.getMessage()));				
			} finally {
            	if ( osw != null ) {
            		try { 
            			osw.flush();
            			osw.close();
            		} catch ( Exception ex ) {};
            	}
            	if ( fos != null ) {
            		try { 
            			fos.flush();
            			fos.close();
            		} catch ( Exception ex ) {};
            	}
            }			
		} else {			
			logError(BaseMessages.getString(PKG, "JobWriteToFile.Error.MissinfgFile"));
		}
		
		return result;
	}

	private void createParentFolder(String realFilename) throws KettleException {
		FileObject parent= null;
		try {
			parent= KettleVFS.getFileObject(realFilename).getParent();
			if(!parent.exists()) {
				if(isCreateParentFolder()) {
					if(isDetailed()) logDetailed(BaseMessages.getString(PKG, "JobWriteToFile.Log.ParentFoldetNotExist",parent.getName().toString()));
					parent.createFolder();
					if(isDetailed()) logDetailed(BaseMessages.getString(PKG, "JobWriteToFile.Log.ParentFolderCreated", parent.getName().toString()));
				}else {
					throw new KettleException(BaseMessages.getString(PKG, "JobWriteToFile.Log.ParentFoldetNotExist", parent.getName().toString()));
				}
			}
		}catch(Exception e) {
			throw new KettleException(BaseMessages.getString(PKG, "JobWriteToFile.Error.CheckingParentFolder", realFilename), e);
		}finally {
			if(parent!=null) {
				try {
					parent.close();
				}catch(Exception e){};
			}
		}
	}
	public boolean evaluates()
	{
		return true;
	}

    public boolean isAppendFile() {
		return appendFile;
	}

	public void setAppendFile(boolean appendFile) {
		this.appendFile = appendFile;
	}
    
	public boolean isCreateParentFolder() {
		return createParentFolder;
	}

	public void setCreateParentFolder(boolean createParentFolder) {
		this.createParentFolder = createParentFolder;
	}
	  public List<ResourceReference> getResourceDependencies(JobMeta jobMeta) {
		    List<ResourceReference> references = super.getResourceDependencies(jobMeta);
		    if (!Const.isEmpty(getFilename())) {
		      String realFileName = jobMeta.environmentSubstitute(getFilename());
		      ResourceReference reference = new ResourceReference(this);
		      reference.getEntries().add( new ResourceEntry(realFileName, ResourceType.FILE));
		      references.add(reference);
		    }
		    return references;
		  }

		  @Override
		  public void check(List<CheckResultInterface> remarks, JobMeta jobMeta)
		  {
		    andValidator().validate(this, "filename", remarks, putValidators(notBlankValidator())); //$NON-NLS-1$
		  }
}