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

package org.pentaho.di.job.entries.deletefiles;

import static org.pentaho.di.job.entry.validator.AbstractFileValidator.putVariableSpace;
import static org.pentaho.di.job.entry.validator.AndValidator.putValidators;
import static org.pentaho.di.job.entry.validator.JobEntryValidatorUtils.andValidator;
import static org.pentaho.di.job.entry.validator.JobEntryValidatorUtils.fileExistsValidator;
import static org.pentaho.di.job.entry.validator.JobEntryValidatorUtils.notNullValidator;

import java.io.IOException;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
import org.pentaho.di.core.logging.LogWriter;
import org.pentaho.di.core.vfs.KettleVFS;
import org.pentaho.di.core.xml.XMLHandler;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.job.Job;
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
 * This defines a 'delete files' job entry.
 *
 * @author Samatar Hassan
 * @since 06-05-2007
 */
public class JobEntryDeleteFiles extends JobEntryBase implements Cloneable, JobEntryInterface {

  private static Class<?> PKG = JobEntryDeleteFiles.class; // for i18n purposes, needed by Translator2!!   $NON-NLS-1$

  public boolean argFromPrevious;

  public boolean includeSubfolders;

  public String arguments[];

  public String filemasks[];

  public JobEntryDeleteFiles(String n) {
    super(n, ""); //$NON-NLS-1$
    argFromPrevious = false;
    arguments = null;

    includeSubfolders = false;
    setID(-1L);
  }

  public JobEntryDeleteFiles() {
    this(""); //$NON-NLS-1$
  }

  public Object clone() {
    JobEntryDeleteFiles je = (JobEntryDeleteFiles) super.clone();
    return je;
  }

  public String getXML() {
    StringBuffer retval = new StringBuffer(300);

    retval.append(super.getXML());
    retval.append("      ").append(XMLHandler.addTagValue("arg_from_previous", argFromPrevious)); //$NON-NLS-1$ //$NON-NLS-2$
    retval.append("      ").append(XMLHandler.addTagValue("include_subfolders", includeSubfolders)); //$NON-NLS-1$ //$NON-NLS-2$

    retval.append("      <fields>").append(Const.CR); //$NON-NLS-1$
    if (arguments != null) {
      for (int i = 0; i < arguments.length; i++) {
        retval.append("        <field>").append(Const.CR); //$NON-NLS-1$
        retval.append("          ").append(XMLHandler.addTagValue("name", arguments[i])); //$NON-NLS-1$ //$NON-NLS-2$
        retval.append("          ").append(XMLHandler.addTagValue("filemask", filemasks[i])); //$NON-NLS-1$ //$NON-NLS-2$
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
      includeSubfolders = "Y".equalsIgnoreCase(XMLHandler.getTagValue(entrynode, "include_subfolders")); //$NON-NLS-1$ //$NON-NLS-2$

      Node fields = XMLHandler.getSubNode(entrynode, "fields"); //$NON-NLS-1$

      // How many field arguments?
      int nrFields = XMLHandler.countNodes(fields, "field"); //$NON-NLS-1$
      arguments = new String[nrFields];
      filemasks = new String[nrFields];

      // Read them all...
      for (int i = 0; i < nrFields; i++) {
        Node fnode = XMLHandler.getSubNodeByNr(fields, "field", i); //$NON-NLS-1$

        arguments[i] = XMLHandler.getTagValue(fnode, "name"); //$NON-NLS-1$
        filemasks[i] = XMLHandler.getTagValue(fnode, "filemask"); //$NON-NLS-1$
      }
    } catch (KettleXMLException xe) {
      throw new KettleXMLException(BaseMessages.getString(PKG, "JobEntryDeleteFiles.UnableToLoadFromXml"), xe); //$NON-NLS-1$
    }
  }

  public void loadRep(Repository rep, ObjectId id_jobentry, List<DatabaseMeta> databases, List<SlaveServer> slaveServers) throws KettleException {
    try {
      argFromPrevious = rep.getJobEntryAttributeBoolean(id_jobentry, "arg_from_previous"); //$NON-NLS-1$
      includeSubfolders = rep.getJobEntryAttributeBoolean(id_jobentry, "include_subfolders"); //$NON-NLS-1$

      // How many arguments?
      int argnr = rep.countNrJobEntryAttributes(id_jobentry, "name"); //$NON-NLS-1$
      arguments = new String[argnr];
      filemasks = new String[argnr];

      // Read them all...
      for (int a = 0; a < argnr; a++) {
        arguments[a] = rep.getJobEntryAttributeString(id_jobentry, a, "name"); //$NON-NLS-1$
        filemasks[a] = rep.getJobEntryAttributeString(id_jobentry, a, "filemask"); //$NON-NLS-1$
      }
    } catch (KettleException dbe) {
      throw new KettleException(BaseMessages.getString(PKG, "JobEntryDeleteFiles.UnableToLoadFromRepo", String.valueOf(id_jobentry)), dbe); //$NON-NLS-1$
    }
  }

  public void saveRep(Repository rep, ObjectId id_job) throws KettleException {
    try {
      rep.saveJobEntryAttribute(id_job, getObjectId(), "arg_from_previous", argFromPrevious); //$NON-NLS-1$
      rep.saveJobEntryAttribute(id_job, getObjectId(), "include_subfolders", includeSubfolders); //$NON-NLS-1$

      // save the arguments...
      if (arguments != null) {
        for (int i = 0; i < arguments.length; i++) {
          rep.saveJobEntryAttribute(id_job, getObjectId(), i, "name", arguments[i]); //$NON-NLS-1$
          rep.saveJobEntryAttribute(id_job, getObjectId(), i, "filemask", filemasks[i]); //$NON-NLS-1$
        }
      }
    } catch (KettleDatabaseException dbe) {
      throw new KettleException(
          BaseMessages.getString(PKG, "JobEntryDeleteFiles.UnableToSaveToRepo", String.valueOf(id_job)), dbe); //$NON-NLS-1$
    }
  }

  public Result execute(Result result, int nr) throws KettleException {
    LogWriter log = LogWriter.getInstance();

    List<RowMetaAndData> rows = result.getRows();
    RowMetaAndData resultRow = null;

    int NrErrFiles = 0;

    result.setResult(false);
    result.setNrErrors(1);


    if (argFromPrevious) {
      if(log.isDetailed())	
    	  logDetailed(BaseMessages.getString(PKG, "JobEntryDeleteFiles.FoundPreviousRows", String.valueOf((rows != null ? rows.size() : 0)))); //$NON-NLS-1$
    }

    if (argFromPrevious && rows != null) // Copy the input row to the (command line) arguments
    {
      for (int iteration = 0; iteration < rows.size() && !parentJob.isStopped(); iteration++) {
        resultRow = rows.get(iteration);

        String args_previous = resultRow.getString(0, null);
        String fmasks_previous = resultRow.getString(1, null);

          // ok we can process this file/folder
          if(log.isDetailed())	
        	  logDetailed(BaseMessages.getString(PKG, "JobEntryDeleteFiles.ProcessingRow", args_previous, fmasks_previous)); //$NON-NLS-1$

          if (!ProcessFile(args_previous, fmasks_previous,parentJob)) {
        	  NrErrFiles++;
          }
      }
    } else if (arguments != null) {

      for (int i = 0; i < arguments.length && !parentJob.isStopped(); i++) {
        
          // ok we can process this file/folder
    	  if(log.isDetailed())	
            logDetailed(BaseMessages.getString(PKG, "JobEntryDeleteFiles.ProcessingArg", arguments[i], filemasks[i])); //$NON-NLS-1$
          if (!ProcessFile(arguments[i], filemasks[i],parentJob)) {
        	  NrErrFiles++;
          }
      }
    }
   
 
    if (NrErrFiles==0)
    {
    	result.setResult(true);
    	result.setNrErrors(0);
    }else
    {
    	result.setNrErrors(NrErrFiles);
    	result.setResult(false);
    }
    

    return result;
  }

  private boolean ProcessFile(String filename, String wildcard,Job parentJob) {
    LogWriter log = LogWriter.getInstance();

    boolean rcode = false;
    FileObject filefolder = null;
    String realFilefoldername = environmentSubstitute(filename);
    String realwildcard = environmentSubstitute(wildcard);

    try {
      filefolder = KettleVFS.getFileObject(realFilefoldername, this);

      // Here gc() is explicitly called if e.g. createfile is used in the same
      // job for the same file. The problem is that after creating the file the
      // file object is not properly garbaged collected and thus the file cannot
      // be deleted anymore. This is a known problem in the JVM.

      System.gc();

      if (filefolder.exists()) {
        // the file or folder exists
        if (filefolder.getType() == FileType.FOLDER) 
        {
          // It's a folder
          if (log.isDetailed())
            logDetailed(BaseMessages.getString(PKG, "JobEntryDeleteFiles.ProcessingFolder", realFilefoldername)); //$NON-NLS-1$
          // Delete Files
          
          int Nr = filefolder.delete(new TextFileSelector(filefolder.toString(),realwildcard,parentJob));

          if (log.isDetailed())
            logDetailed(BaseMessages.getString(PKG, "JobEntryDeleteFiles.TotalDeleted", String.valueOf(Nr))); //$NON-NLS-1$
          rcode = true;
        } else {
          // It's a file
          if(log.isDetailed())	
        	  logDetailed(BaseMessages.getString(PKG, "JobEntryDeleteFiles.ProcessingFile", realFilefoldername)); //$NON-NLS-1$
          boolean deleted = filefolder.delete();
          if (!deleted) {
            logError(BaseMessages.getString(PKG, "JobEntryDeleteFiles.CouldNotDeleteFile", realFilefoldername)); //$NON-NLS-1$
          } else {
            if(log.isBasic()) logBasic(BaseMessages.getString(PKG, "JobEntryDeleteFiles.FileDeleted", filename)); //$NON-NLS-1$
            rcode = true;
          }
        }
      } else {
        // File already deleted, no reason to try to delete it
    	  if(log.isBasic()) logBasic(BaseMessages.getString(PKG, "JobEntryDeleteFiles.FileAlreadyDeleted", realFilefoldername)); //$NON-NLS-1$
        rcode = true;
      }
    } catch (Exception e) {
      logError(BaseMessages.getString(PKG, "JobEntryDeleteFiles.CouldNotProcess", realFilefoldername, e.getMessage()), e); //$NON-NLS-1$
    } finally {
      if (filefolder != null) {
        try {
          filefolder.close();
        } catch (IOException ex) {
        }
        ;
      }
    }

    return rcode;
  }


	private class TextFileSelector implements FileSelector 
	{
		String file_wildcard=null,source_folder=null;
		Job parentjob;
		
		public TextFileSelector(String sourcefolderin,String filewildcard,Job parentJob) 
		 {
			
			 if ( !Const.isEmpty(sourcefolderin))
			 {
				 source_folder=sourcefolderin;
			 }
			
			 if ( !Const.isEmpty(filewildcard))
			 {
				 file_wildcard=filewildcard;
			 }
			 parentjob=parentJob;
		 }
		 
		public boolean includeFile(FileSelectInfo info) 
		{
			boolean returncode=false;
			FileObject file_name=null;
			try
			{
				
				if (!info.getFile().toString().equals(source_folder) && !parentjob.isStopped())
				{
					// Pass over the Base folder itself
					
					String short_filename= info.getFile().getName().getBaseName();
					
					if (!info.getFile().getParent().equals(info.getBaseFolder()))
					 {
						
						// Not in the Base Folder..Only if include sub folders  
						 if (includeSubfolders && (info.getFile().getType() == FileType.FILE) && GetFileWildcard(short_filename,file_wildcard))
						 {
							if (log.isDetailed()) 
								logDetailed(BaseMessages.getString(PKG, "JobEntryDeleteFiles.DeletingFile",info.getFile().toString())); //$NON-NLS-1$

							returncode= true; 				
							 
						 }
					 }
					 else
					 {
						// In the Base Folder...
						 
						 if ((info.getFile().getType() == FileType.FILE) && GetFileWildcard(short_filename,file_wildcard))
						 {
							if (log.isDetailed())
								logDetailed(BaseMessages.getString(PKG, "JobEntryDeleteFiles.DeletingFile",info.getFile().toString())); //$NON-NLS-1$
							
							returncode= true; 				
							 
						 }
						
					 }
					
				}
				
			}
			catch (Exception e) 
			{
				

				log.logError(BaseMessages.getString(PKG, "JobDeleteFiles.Error.Exception.DeleteProcessError") , BaseMessages.getString(PKG, "JobDeleteFiles.Error.Exception.DeleteProcess", 
					info.getFile().toString(),e.getMessage()));
				
				 returncode= false;
			}
			finally 
			{
				if ( file_name != null )
				{
					try  
					{
						file_name.close();
						
					}
					catch ( IOException ex ) {};
				}
				
				
				
			}
			
			
			return returncode;
		}

		public boolean traverseDescendents(FileSelectInfo info) 
		{
			return true;
		}
	}
  
	/**********************************************************
	 * 
	 * @param selectedfile
	 * @param wildcard
	 * @return True if the selectedfile matches the wildcard
	 **********************************************************/
	private boolean GetFileWildcard(String selectedfile, String wildcard)
	{
		Pattern pattern = null;
		boolean getIt=true;
	
        if (!Const.isEmpty(wildcard))
        {
        	 pattern = Pattern.compile(wildcard);
			// First see if the file matches the regular expression!
			if (pattern!=null)
			{
				Matcher matcher = pattern.matcher(selectedfile);
				getIt = matcher.matches();
			}
        }
		
		return getIt;
	}



  public void setIncludeSubfolders(boolean includeSubfolders) {
    this.includeSubfolders = includeSubfolders;
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



  public String[] getFilemasks()
  {
    return filemasks;
  }

  public boolean isIncludeSubfolders()
  {
    return includeSubfolders;
  }

}