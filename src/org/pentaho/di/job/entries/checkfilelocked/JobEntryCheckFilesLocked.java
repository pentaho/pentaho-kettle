 /* Copyright (c) 2007 Pentaho Corporation.  All rights reserved. 
 * This software was developed by Pentaho Corporation and is provided under the terms 
 * of the GNU Lesser General Public License, Version 2.1. You may not use 
 * this file except in compliance with the license. If you need a copy of the license, 
 * please go to http://www.gnu.org/licenses/lgpl-2.1.txt. The Original Code is Pentaho 
 * Data Integration.  The Initial Developer is Samatar Hassan.
 *
 * Software distributed under the GNU Lesser Public License is distributed on an "AS IS" 
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or  implied. Please refer to 
 * the license for the specific language governing your rights and limitations.*/
 
package org.pentaho.di.job.entries.checkfilelocked;

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
import org.pentaho.di.core.vfs.KettleVFS;
import org.pentaho.di.core.xml.XMLHandler;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.job.JobMeta;
import org.pentaho.di.job.entry.JobEntryBase;
import org.pentaho.di.job.entry.JobEntryInterface;
import org.pentaho.di.job.entry.validator.ValidatorContext;
import org.pentaho.di.repository.ObjectId;
import org.pentaho.di.repository.Repository;
import org.w3c.dom.Node;


/**
 * This defines a 'check files locked' job entry.
 *
 * @author Samatar Hassan
 * @since 06-05-2007
 */

public class JobEntryCheckFilesLocked extends JobEntryBase implements Cloneable, JobEntryInterface
{
  private static Class<?> PKG = JobEntryCheckFilesLocked.class; // for i18n purposes, needed by Translator2!!   $NON-NLS-1$
	
  public boolean argFromPrevious;

  public boolean includeSubfolders;

  public String arguments[];

  public String filemasks[];
  
  private boolean oneFileLocked;

  public JobEntryCheckFilesLocked(String n) {
    super(n, ""); //$NON-NLS-1$
    argFromPrevious = false;
    arguments = null;

    includeSubfolders = false;
    setID(-1L);

  }

  public JobEntryCheckFilesLocked() {
    this(""); //$NON-NLS-1$
  }

  public Object clone() {
    JobEntryCheckFilesLocked je = (JobEntryCheckFilesLocked) super.clone();
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

	public void loadXML(Node entrynode, List<DatabaseMeta>  databases, List<SlaveServer> slaveServers, Repository rep) throws KettleXMLException
	{
	  try
	  {
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
      throw new KettleXMLException(BaseMessages.getString(PKG, "JobEntryCheckFilesLocked.UnableToLoadFromXml"), xe); //$NON-NLS-1$
    }
  }

	public void loadRep(Repository rep, ObjectId id_jobentry, List<DatabaseMeta> databases, List<SlaveServer> slaveServers) throws KettleException
	{
	try
	{
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
      throw new KettleException(BaseMessages.getString(PKG, "JobEntryCheckFilesLocked.UnableToLoadFromRepo", String.valueOf(id_jobentry)), dbe); //$NON-NLS-1$
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
          BaseMessages.getString(PKG, "JobEntryCheckFilesLocked.UnableToSaveToRepo", String.valueOf(id_job)), dbe); //$NON-NLS-1$
    }
  }

  public Result execute(Result previousResult, int nr) {

	Result result = previousResult;
    List<RowMetaAndData> rows = result.getRows();
    RowMetaAndData resultRow = null;
    
    oneFileLocked=false;
    result.setResult(true);

    try {
	    if (argFromPrevious) {
	      if(isDetailed()) 
	    	  logDetailed( BaseMessages.getString(PKG, "JobEntryCheckFilesLocked.FoundPreviousRows", String.valueOf((rows != null ? rows.size() : 0)))); //$NON-NLS-1$
	    }
	
	    if (argFromPrevious && rows != null)  {
	      // Copy the input row to the (command line) arguments
	      for (int iteration = 0; iteration < rows.size() && !parentJob.isStopped(); iteration++) {
	    	  resultRow = (RowMetaAndData) rows.get(iteration);
	
	    	 // Get values from previous result 
	        String filefolder_previous = resultRow.getString(0, "");
	        String fmasks_previous = resultRow.getString(1, "");        
	
	          // ok we can process this file/folder
	          if(isDetailed()) 
	        	  logDetailed( BaseMessages.getString(PKG, "JobEntryCheckFilesLocked.ProcessingRow", filefolder_previous, fmasks_previous)); //$NON-NLS-1$
	
	          ProcessFile(filefolder_previous, fmasks_previous);
	      }
	    } else if (arguments != null) {
	
	      for (int i = 0; i < arguments.length && !parentJob.isStopped(); i++) {
	          // ok we can process this file/folder
	    	  if(isDetailed()) 
	    		  logDetailed( BaseMessages.getString(PKG, "JobEntryCheckFilesLocked.ProcessingArg", arguments[i], filemasks[i])); //$NON-NLS-1$
	         
	    	  ProcessFile(arguments[i], filemasks[i]);
	      }
	    }

	    if (oneFileLocked) {
	    	result.setResult(false);
	    	result.setNrErrors(1);
	    }    
    }catch(Exception e) {
    	logError(BaseMessages.getString(PKG, "JobEntryCheckFilesLocked.ErrorRunningJobEntry", e));
    }

    return result;
  }

  private void ProcessFile(String filename, String wildcard) {

    FileObject filefolder = null;
    String realFilefoldername = environmentSubstitute(filename);
    String realwilcard = environmentSubstitute(wildcard);

    try {
      filefolder = KettleVFS.getFileObject(realFilefoldername);
      FileObject[] files = new FileObject[]{filefolder};
      if (filefolder.exists()) {
        // the file or folder exists
        if (filefolder.getType() == FileType.FOLDER) {
          // It's a folder
          if (isDetailed())
            logDetailed( BaseMessages.getString(PKG, "JobEntryCheckFilesLocked.ProcessingFolder", realFilefoldername)); //$NON-NLS-1$
          // Retrieve all files
          files = filefolder.findFiles(new TextFileSelector(filefolder.toString(),realwilcard));

          if (isDetailed())
            logDetailed( BaseMessages.getString(PKG, "JobEntryCheckFilesLocked.TotalFilesToCheck", String.valueOf(files.length))); //$NON-NLS-1$;
        } else {
          // It's a file
          if(isDetailed()) 
        	  logDetailed( BaseMessages.getString(PKG, "JobEntryCheckFilesLocked.ProcessingFile", realFilefoldername)); //$NON-NLS-1$
        }
        // Check files locked
        checkFilesLocked(files);
      } else {
        // We can not find thsi file
        logBasic( BaseMessages.getString(PKG, "JobEntryCheckFilesLocked.FileNotExist", realFilefoldername)); //$NON-NLS-1$
      }
    } catch (Exception e) {
      logError( BaseMessages.getString(PKG, "JobEntryCheckFilesLocked.CouldNotProcess", realFilefoldername, e.getMessage())); //$NON-NLS-1$
    } finally {
      if (filefolder != null) {
        try {
          filefolder.close();
        } catch (IOException ex) {};
      }
    }
  }

  private void checkFilesLocked(FileObject[] files) throws KettleException {
	  
	  for(int i=0; i<files.length && !oneFileLocked; i++) {
		  FileObject file = files[i];
		  String filename = KettleVFS.getFilename(file);
		  LockFile locked = new LockFile(filename);
		  if(locked.isLocked()) {
			  oneFileLocked= true;
			  logError( BaseMessages.getString(PKG, "JobCheckFilesLocked.Log.FileLocked",filename));
		  }else {
			  if(isDetailed()) logDetailed( BaseMessages.getString(PKG, "JobCheckFilesLocked.Log.FileNotLocked",filename));  
		  }
	  }
  }

	private class TextFileSelector implements FileSelector 
	{
		String file_wildcard=null,source_folder=null;
		
		public TextFileSelector(String sourcefolderin,String filewildcard) 
		 {
			
			 if ( !Const.isEmpty(sourcefolderin))
			 {
				 source_folder=sourcefolderin;
			 }
			
			 if ( !Const.isEmpty(filewildcard))
			 {
				 file_wildcard=filewildcard;
			 }
		 }
		 
		public boolean includeFile(FileSelectInfo info) 
		{
			boolean returncode=false;
			FileObject file_name=null;
			try
			{
				
				if (!info.getFile().toString().equals(source_folder))
				{
					// Pass over the Base folder itself
					
					String short_filename= info.getFile().getName().getBaseName();
					
					if (!info.getFile().getParent().equals(info.getBaseFolder()))
					 {
						
						// Not in the Base Folder..Only if include sub folders  
						 if (includeSubfolders && (info.getFile().getType() == FileType.FILE) && GetFileWildcard(short_filename,file_wildcard))
						 {
							if (isDetailed())
								logDetailed( BaseMessages.getString(PKG, "JobEntryCheckFilesLocked.CheckingFile",info.getFile().toString())); //$NON-NLS-1$
							
							returncode= true; 				
							 
						 }
					 }
					 else
					 {
						// In the Base Folder...
						 
						 if ((info.getFile().getType() == FileType.FILE) && GetFileWildcard(short_filename,file_wildcard))
						 {
							if (isDetailed())
								logDetailed( BaseMessages.getString(PKG, "JobEntryCheckFilesLocked.CheckingFile",info.getFile().toString())); //$NON-NLS-1$
							
							returncode= true; 				
							 
						 }
					 }
				}
				
			}
			catch (Exception e) 
			{
				logError(BaseMessages.getString(PKG, "JobCheckFilesLocked.Error.Exception.ProcessError") , 
						BaseMessages.getString(PKG, "JobCheckFilesLocked.Error.Exception.Process", 
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
			return info.getDepth()==0 || includeSubfolders;
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
  public void setargFromPrevious(boolean argFromPrevious) {
	    this.argFromPrevious = argFromPrevious;
	  }

  
  
  
  public boolean evaluates() {
    return true;
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
	
  public void check(List<CheckResultInterface> remarks, JobMeta jobMeta) 
  {
	    boolean res = andValidator().validate(this, "arguments", remarks, putValidators(notNullValidator())); 

	    if (res == false) 
	    {
	      return;
	    }

	    ValidatorContext ctx = new ValidatorContext();
	    putVariableSpace(ctx, getVariables());
	    putValidators(ctx, notNullValidator(), fileExistsValidator());

	    for (int i = 0; i < arguments.length; i++) 
	    {
	      andValidator().validate(this, "arguments[" + i + "]", remarks, ctx);
	    } 
	  }

}