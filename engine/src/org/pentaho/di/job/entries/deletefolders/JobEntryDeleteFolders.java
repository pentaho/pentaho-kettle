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

package org.pentaho.di.job.entries.deletefolders;

import static org.pentaho.di.job.entry.validator.AbstractFileValidator.putVariableSpace;
import static org.pentaho.di.job.entry.validator.AndValidator.putValidators;
import static org.pentaho.di.job.entry.validator.JobEntryValidatorUtils.andValidator;
import static org.pentaho.di.job.entry.validator.JobEntryValidatorUtils.fileExistsValidator;
import static org.pentaho.di.job.entry.validator.JobEntryValidatorUtils.notNullValidator;

import java.io.IOException;
import java.util.List;

import org.apache.commons.vfs.FileObject;
import org.apache.commons.vfs.FileSelectInfo;
import org.apache.commons.vfs.FileSelector;
import org.apache.commons.vfs.FileType;
import org.pentaho.di.cluster.SlaveServer;
import org.pentaho.di.core.CheckResultInterface;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.Result;
import org.pentaho.di.core.RowMetaAndData;
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
import org.pentaho.di.job.entry.validator.ValidatorContext;
import org.pentaho.di.repository.ObjectId;
import org.pentaho.di.repository.Repository;
import org.pentaho.di.resource.ResourceEntry;
import org.pentaho.di.resource.ResourceReference;
import org.pentaho.di.resource.ResourceEntry.ResourceType;
import org.w3c.dom.Node;

/**
 * This defines a 'delete folders' job entry.
 *
 * @author Samatar Hassan
 * @since 13-05-2008
 */
public class JobEntryDeleteFolders extends JobEntryBase implements Cloneable, JobEntryInterface {
  private static Class<?> PKG = JobEntryDeleteFolders.class; // for i18n purposes, needed by Translator2!!   $NON-NLS-1$

  public boolean argFromPrevious;

  public String arguments[];
  
  private String success_condition;
  public  String SUCCESS_IF_AT_LEAST_X_FOLDERS_DELETED="success_when_at_least";
  public  String SUCCESS_IF_ERRORS_LESS="success_if_errors_less";
  public  String SUCCESS_IF_NO_ERRORS="success_if_no_errors";
  
  private String limit_folders;
  
	
	int NrErrors=0;
	int NrSuccess=0;
	boolean successConditionBroken=false;
	boolean successConditionBrokenExit=false;
	int limitFolders=0;


  public JobEntryDeleteFolders(String n) {
    super(n, ""); //$NON-NLS-1$
    argFromPrevious = false;
    arguments = null;

    success_condition=SUCCESS_IF_NO_ERRORS;
    limit_folders="10";
    setID(-1L);
  }

  public JobEntryDeleteFolders() {
    this(""); //$NON-NLS-1$
  }

  public Object clone() {
    JobEntryDeleteFolders je = (JobEntryDeleteFolders) super.clone();
    return je;
  }

  public String getXML() {
    StringBuffer retval = new StringBuffer(300);

    retval.append(super.getXML());
    retval.append("      ").append(XMLHandler.addTagValue("arg_from_previous", argFromPrevious)); //$NON-NLS-1$ //$NON-NLS-2$
    retval.append("      ").append(XMLHandler.addTagValue("success_condition", success_condition)); //$NON-NLS-1$ //$NON-NLS-2$
	retval.append("      ").append(XMLHandler.addTagValue("limit_folders", limit_folders));
	
    retval.append("      <fields>").append(Const.CR); //$NON-NLS-1$
    if (arguments != null) {
      for (int i = 0; i < arguments.length; i++) {
        retval.append("        <field>").append(Const.CR); //$NON-NLS-1$
        retval.append("          ").append(XMLHandler.addTagValue("name", arguments[i])); //$NON-NLS-1$ //$NON-NLS-2$
        retval.append("        </field>").append(Const.CR); //$NON-NLS-1$
      }
    }
    retval.append("      </fields>").append(Const.CR); //$NON-NLS-1$

    return retval.toString();
  }

  public void loadXML(Node entrynode, List<DatabaseMeta> databases, List<SlaveServer> slaveServers, Repository rep) throws KettleXMLException {
    try {
      super.loadXML(entrynode, databases, slaveServers);
      argFromPrevious = "Y".equalsIgnoreCase(XMLHandler.getTagValue(entrynode, "arg_from_previous")); //$NON-NLS-1$ //$NON-NLS-2$
      success_condition          = XMLHandler.getTagValue(entrynode, "success_condition");
      limit_folders          = XMLHandler.getTagValue(entrynode, "limit_folders");
      
      Node fields = XMLHandler.getSubNode(entrynode, "fields"); //$NON-NLS-1$

      // How many field arguments?
      int nrFields = XMLHandler.countNodes(fields, "field"); //$NON-NLS-1$
      arguments = new String[nrFields];

      // Read them all...
      for (int i = 0; i < nrFields; i++) {
        Node fnode = XMLHandler.getSubNodeByNr(fields, "field", i); //$NON-NLS-1$

        arguments[i] = XMLHandler.getTagValue(fnode, "name"); //$NON-NLS-1$
      }
    } catch (KettleXMLException xe) {
      throw new KettleXMLException(BaseMessages.getString(PKG, "JobEntryDeleteFolders.UnableToLoadFromXml"), xe); //$NON-NLS-1$
    }
  }

  public void loadRep(Repository rep, ObjectId id_jobentry, List<DatabaseMeta> databases, List<SlaveServer> slaveServers) throws KettleException {
    try {
      argFromPrevious = rep.getJobEntryAttributeBoolean(id_jobentry, "arg_from_previous"); //$NON-NLS-1$
      limit_folders  = rep.getJobEntryAttributeString(id_jobentry, "limit_folders");
		success_condition  = rep.getJobEntryAttributeString(id_jobentry, "success_condition");

      // How many arguments?
      int argnr = rep.countNrJobEntryAttributes(id_jobentry, "name"); //$NON-NLS-1$
      arguments = new String[argnr];

      // Read them all...
      for (int a = 0; a < argnr; a++) {
        arguments[a] = rep.getJobEntryAttributeString(id_jobentry, a, "name"); //$NON-NLS-1$
      }
    } catch (KettleException dbe) {
      throw new KettleException(BaseMessages.getString(PKG, "JobEntryDeleteFolders.UnableToLoadFromRepo", String.valueOf(id_jobentry)), dbe); //$NON-NLS-1$
    }
  }

  public void saveRep(Repository rep, ObjectId id_job) throws KettleException {
    try {
      rep.saveJobEntryAttribute(id_job, getObjectId(), "arg_from_previous", argFromPrevious); //$NON-NLS-1$
	  rep.saveJobEntryAttribute(id_job, getObjectId(), "limit_folders",      limit_folders);
	  rep.saveJobEntryAttribute(id_job, getObjectId(), "success_condition",      success_condition);

      // save the arguments...
      if (arguments != null) {
        for (int i = 0; i < arguments.length; i++) {
          rep.saveJobEntryAttribute(id_job, getObjectId(), i, "name", arguments[i]); //$NON-NLS-1$
        }
      }
    } catch (KettleDatabaseException dbe) {
      throw new KettleException(
          BaseMessages.getString(PKG, "JobEntryDeleteFolders.UnableToSaveToRepo", String.valueOf(id_job)), dbe); //$NON-NLS-1$
    }
  }

  public Result execute(Result result, int nr) throws KettleException {
    List<RowMetaAndData> rows = result.getRows();
    RowMetaAndData resultRow = null;

    result.setNrErrors(1);
    result.setResult(false);
    
	NrErrors=0;
	NrSuccess=0;
	successConditionBroken=false;
	successConditionBrokenExit=false;
	limitFolders=Const.toInt(environmentSubstitute(getLimitFolders()),10);


    if (argFromPrevious) {
      if(log.isDetailed())	
    	  logDetailed(BaseMessages.getString(PKG, "JobEntryDeleteFolders.FoundPreviousRows", String.valueOf((rows != null ? rows.size() : 0)))); //$NON-NLS-1$
    }

    if (argFromPrevious && rows != null){
      for (int iteration = 0; iteration < rows.size() && !parentJob.isStopped(); iteration++) {
		if(successConditionBroken){
			logError(BaseMessages.getString(PKG, "JobEntryDeleteFolders.Error.SuccessConditionbroken",""+NrErrors));
			result.setNrErrors(NrErrors);
			result.setNrLinesDeleted(NrSuccess);
			return result;
		}
    	resultRow = rows.get(iteration);
        String args_previous = resultRow.getString(0, null);
        if(!Const.isEmpty(args_previous)){
	        if(deleteFolder(args_previous)){
	        	updateSuccess();
	        }else {
	        	updateErrors();	
	        }
        }else{
        	// empty filename !
        	logError(BaseMessages.getString(PKG, "JobEntryDeleteFolders.Error.EmptyLine"));
        }
      }
    } else if (arguments != null) {
      for (int i = 0; i < arguments.length && !parentJob.isStopped(); i++) {
  		if(successConditionBroken)
		{
			logError(BaseMessages.getString(PKG, "JobEntryDeleteFolders.Error.SuccessConditionbroken",""+NrErrors));
			result.setNrErrors(NrErrors);
			result.setNrLinesDeleted(NrSuccess);
			return result;
		}
  		String realfilename=environmentSubstitute(arguments[i]);
	    if(!Const.isEmpty(realfilename))
	    {
    	  if(deleteFolder(realfilename)){
          	updateSuccess();
          }else {
        	  updateErrors();
          }  
	    }else{
         // empty filename !
         logError(BaseMessages.getString(PKG, "JobEntryDeleteFolders.Error.EmptyLine"));
	   }
      }
    }
   
	if(log.isDetailed()){
		logDetailed("=======================================");
		logDetailed(BaseMessages.getString(PKG, "JobEntryDeleteFolders.Log.Info.NrError","" + NrErrors));
		logDetailed(BaseMessages.getString(PKG, "JobEntryDeleteFolders.Log.Info.NrDeletedFolders","" + NrSuccess));
		logDetailed("=======================================");
	}
    
	result.setNrErrors(NrErrors);
	result.setNrLinesDeleted(NrSuccess);
	if(getSuccessStatus())	result.setResult(true);
	
    return result;
  }
	private void updateErrors()
	{
		NrErrors++;
		if(checkIfSuccessConditionBroken()){
			// Success condition was broken
			successConditionBroken=true;
		}
	}
	private boolean checkIfSuccessConditionBroken()
	{
		boolean retval=false;
		if ((NrErrors>0 && getSuccessCondition().equals(SUCCESS_IF_NO_ERRORS))
				|| (NrErrors>=limitFolders && getSuccessCondition().equals(SUCCESS_IF_ERRORS_LESS))){
			retval=true;	
		}
		return retval;
	}
	private void updateSuccess()
	{
		NrSuccess++;
	}
	private boolean getSuccessStatus()
	{
		boolean retval=false;
		
		if ((NrErrors==0 && getSuccessCondition().equals(SUCCESS_IF_NO_ERRORS))
				|| (NrSuccess>=limitFolders && getSuccessCondition().equals(SUCCESS_IF_AT_LEAST_X_FOLDERS_DELETED))
				|| (NrErrors<=limitFolders && getSuccessCondition().equals(SUCCESS_IF_ERRORS_LESS))){
				retval=true;	
			}
		
		return retval;
	}
  private boolean deleteFolder(String foldername) {
    boolean rcode = false;
    FileObject filefolder = null;

    try {
      filefolder = KettleVFS.getFileObject(foldername, this);

      if (filefolder.exists()) {
        // the file or folder exists
        if (filefolder.getType() == FileType.FOLDER) {
          // It's a folder
          if (log.isDetailed())
            logDetailed(BaseMessages.getString(PKG, "JobEntryDeleteFolders.ProcessingFolder", foldername)); //$NON-NLS-1$
          // Delete Files
          int Nr = filefolder.delete(new TextFileSelector());

          if (log.isDetailed())
            logDetailed(BaseMessages.getString(PKG, "JobEntryDeleteFolders.TotalDeleted", foldername,String.valueOf(Nr))); //$NON-NLS-1$
          rcode = true;
        } else {
        	// Error...This file is not a folder!
        	logError(BaseMessages.getString(PKG, "JobEntryDeleteFolders.Error.NotFolder"));
        }
      } else {
        // File already deleted, no reason to try to delete it
    	  if(log.isBasic()) logBasic(BaseMessages.getString(PKG, "JobEntryDeleteFolders.FolderAlreadyDeleted", foldername)); //$NON-NLS-1$
        rcode = true;
      }
    } catch (Exception e) {
      logError(BaseMessages.getString(PKG, "JobEntryDeleteFolders.CouldNotDelete", foldername, e.getMessage()), e); //$NON-NLS-1$
    } finally {
      if (filefolder != null) {
        try {
          filefolder.close();
          filefolder=null;
        } catch (IOException ex) {
        };
      }
    }

    return rcode;
  }


	private class TextFileSelector implements FileSelector 
	{	
		public boolean includeFile(FileSelectInfo info) {
			return true;
		}

		public boolean traverseDescendents(FileSelectInfo info)	{
			return true;
		}
	}
  

  public void setPrevious(boolean argFromPrevious) {
	    this.argFromPrevious = argFromPrevious;
	  }

  
  
  public boolean evaluates() {
    return true;
  }

  public void check(List<CheckResultInterface> remarks, JobMeta jobMeta) {
    boolean res = andValidator().validate(this, "arguments", remarks, putValidators(notNullValidator())); //$NON-NLS-1$

    if (res == false) {
      return;
    }

    ValidatorContext ctx = new ValidatorContext();
    putVariableSpace(ctx, getVariables());
    putValidators(ctx, notNullValidator(), fileExistsValidator());

    for (int i = 0; i < arguments.length; i++) {
      andValidator().validate(this, "arguments[" + i + "]", remarks, ctx); //$NON-NLS-1$ //$NON-NLS-2$
    }
  }

  public List<ResourceReference> getResourceDependencies(JobMeta jobMeta) {
    List<ResourceReference> references = super.getResourceDependencies(jobMeta);
    if (arguments != null) {
      ResourceReference reference = null;
      for (int i=0; i<arguments.length; i++) {
        String filename = jobMeta.environmentSubstitute(arguments[i]);
        if (reference == null) {
          reference = new ResourceReference(this);
          references.add(reference);
        }
        reference.getEntries().add( new ResourceEntry(filename, ResourceType.FILE));
     }
    }
    return references;
  }

  public boolean isArgFromPrevious()
  {
    return argFromPrevious;
  }

  public String[] getArguments()
  {
    return arguments;
  }

	public void setSuccessCondition(String success_condition)
	{
		this.success_condition=success_condition;
	}
	public String getSuccessCondition()
	{
		return success_condition;
	}
	public void setLimitFolders(String limit_folders)
	{
		this.limit_folders=limit_folders;
	}
	
	public String getLimitFolders()
	{
		return limit_folders;
	}

}