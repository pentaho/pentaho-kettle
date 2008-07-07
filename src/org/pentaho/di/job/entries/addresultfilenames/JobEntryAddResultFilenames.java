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

package org.pentaho.di.job.entries.addresultfilenames;

import static org.pentaho.di.job.entry.validator.AbstractFileValidator.putVariableSpace;
import static org.pentaho.di.job.entry.validator.AndValidator.putValidators;
import static org.pentaho.di.job.entry.validator.JobEntryValidatorUtils.andValidator;
import static org.pentaho.di.job.entry.validator.JobEntryValidatorUtils.fileExistsValidator;
import static org.pentaho.di.job.entry.validator.JobEntryValidatorUtils.notNullValidator;

import java.io.*;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.w3c.dom.Node;
import org.apache.commons.vfs.FileObject;
import org.apache.commons.vfs.FileType;
import org.apache.commons.vfs.FileSelector;
import org.apache.commons.vfs.FileSelectInfo;

import org.pentaho.di.core.ResultFile;
import org.pentaho.di.core.Const;
import org.pentaho.di.cluster.SlaveServer;
import org.pentaho.di.core.CheckResultInterface;
import org.pentaho.di.core.Result;
import org.pentaho.di.core.RowMetaAndData;
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
import org.pentaho.di.job.entry.validator.ValidatorContext;
import org.pentaho.di.repository.Repository;


/**
 * This defines a 'add result filenames' job entry.
 *
 * @author Samatar Hassan
 * @since 06-05-2007
 */
public class JobEntryAddResultFilenames extends JobEntryBase implements Cloneable, JobEntryInterface
{

  public boolean argFromPrevious;
  
  public boolean deleteallbefore;

  public boolean includeSubfolders;

  public String arguments[];

  public String filemasks[];

  public JobEntryAddResultFilenames(String n) {
    super(n, ""); //$NON-NLS-1$
    argFromPrevious = false;
    deleteallbefore=false;
    arguments = null;

    includeSubfolders = false;
    setID(-1L);
    setJobEntryType(JobEntryType.ADD_RESULT_FILENAMES);
  }

  public JobEntryAddResultFilenames() {
    this(""); //$NON-NLS-1$
  }

  public JobEntryAddResultFilenames(JobEntryBase jeb) {
    super(jeb);
  }

  public Object clone() {
    JobEntryAddResultFilenames je = (JobEntryAddResultFilenames) super.clone();
    return je;
  }

  public String getXML() {
    StringBuffer retval = new StringBuffer(300);

    retval.append(super.getXML());
    retval.append("      ").append(XMLHandler.addTagValue("arg_from_previous", argFromPrevious)); //$NON-NLS-1$ //$NON-NLS-2$
    retval.append("      ").append(XMLHandler.addTagValue("include_subfolders", includeSubfolders)); //$NON-NLS-1$ //$NON-NLS-2$
    retval.append("      ").append(XMLHandler.addTagValue("delete_all_before", deleteallbefore));
    
    
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

  public void loadXML(Node entrynode, List<DatabaseMeta> databases, List<SlaveServer> slaveServers, Repository rep) throws KettleXMLException
  {
    try
    {
      super.loadXML(entrynode, databases, slaveServers);
      argFromPrevious = "Y".equalsIgnoreCase(XMLHandler.getTagValue(entrynode, "arg_from_previous")); //$NON-NLS-1$ //$NON-NLS-2$
      includeSubfolders = "Y".equalsIgnoreCase(XMLHandler.getTagValue(entrynode, "include_subfolders")); //$NON-NLS-1$ //$NON-NLS-2$
      deleteallbefore = "Y".equalsIgnoreCase(XMLHandler.getTagValue(entrynode, "delete_all_before"));
      
      
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
      throw new KettleXMLException(Messages.getString("JobEntryAddResultFilenames.UnableToLoadFromXml"), xe); //$NON-NLS-1$
    }
  }

  public void loadRep(Repository rep, long id_jobentry, List<DatabaseMeta> databases, List<SlaveServer> slaveServers) throws KettleException
  {
    try
    {
      super.loadRep(rep, id_jobentry, databases, slaveServers);
      argFromPrevious = rep.getJobEntryAttributeBoolean(id_jobentry, "arg_from_previous"); //$NON-NLS-1$
      includeSubfolders = rep.getJobEntryAttributeBoolean(id_jobentry, "include_subfolders"); //$NON-NLS-1$

      deleteallbefore = rep.getJobEntryAttributeBoolean(id_jobentry, "delete_all_before");
      
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
      throw new KettleException(Messages.getString("JobEntryAddResultFilenames.UnableToLoadFromRepo", String.valueOf(id_jobentry)), dbe); //$NON-NLS-1$
    }
  }

  public void saveRep(Repository rep, long id_job) throws KettleException {
    try {
      super.saveRep(rep, id_job);

      rep.saveJobEntryAttribute(id_job, getID(), "arg_from_previous", argFromPrevious); //$NON-NLS-1$
      rep.saveJobEntryAttribute(id_job, getID(), "include_subfolders", includeSubfolders); //$NON-NLS-1$
      rep.saveJobEntryAttribute(id_job, getID(), "delete_all_before", deleteallbefore);
      
      
      // save the arguments...
      if (arguments != null) {
        for (int i = 0; i < arguments.length; i++) {
          rep.saveJobEntryAttribute(id_job, getID(), i, "name", arguments[i]); //$NON-NLS-1$
          rep.saveJobEntryAttribute(id_job, getID(), i, "filemask", filemasks[i]); //$NON-NLS-1$
        }
      }
    } catch (KettleDatabaseException dbe) {
      throw new KettleException(Messages.getString("JobEntryAddResultFilenames.UnableToSaveToRepo", String.valueOf(id_job)), dbe); //$NON-NLS-1$
    }
  }

  public Result execute(Result result, int nr, Repository rep, Job parentJob) throws KettleException {
    LogWriter log = LogWriter.getInstance();
    List<RowMetaAndData> rows = result.getRows();
    RowMetaAndData resultRow = null;

    int NrErrFiles = 0;
    result.setResult(true);


    if(deleteallbefore)
	{
    	// clear result filenames
    	int size=result.getResultFiles().size();
    	if(log.isBasic()) log.logBasic(toString(),Messages.getString("JobEntryAddResultFilenames.log.FilesFound",""+size));
	
		result.getResultFiles().clear();
		if(log.isDetailed()) log.logDetailed(toString(),Messages.getString("JobEntryAddResultFilenames.log.DeletedFiles",""+size));
	}
    
    
    if (argFromPrevious) 
    {
    	if(log.isDetailed()) 
    		log.logDetailed(toString(), Messages.getString("JobEntryAddResultFilenames.FoundPreviousRows", String.valueOf((rows != null ? rows.size() : 0)))); //$NON-NLS-1$
    }

    if (argFromPrevious && rows != null) // Copy the input row to the (command line) arguments
    {   	        
      for (int iteration = 0; iteration < rows.size() && !parentJob.isStopped(); iteration++) {
    	  resultRow = rows.get(iteration);

    	 // Get values from previous result 
        String filefolder_previous = resultRow.getString(0,null);
        String fmasks_previous = resultRow.getString(1,null);       

         // ok we can process this file/folder
        if(log.isDetailed()) log.logDetailed(toString(), Messages.getString("JobEntryAddResultFilenames.ProcessingRow", filefolder_previous, fmasks_previous)); //$NON-NLS-1$

          if (!ProcessFile(filefolder_previous, fmasks_previous,parentJob,result)) {
        	  NrErrFiles = NrErrFiles++;
          }
       
      }
    } else if (arguments != null) {

      for (int i = 0; i < arguments.length  && !parentJob.isStopped(); i++) {
        
          // ok we can process this file/folder
    	  if(log.isDetailed()) log.logDetailed(toString(), Messages.getString("JobEntryAddResultFilenames.ProcessingArg", arguments[i], filemasks[i])); //$NON-NLS-1$
          if (!ProcessFile(arguments[i], filemasks[i],parentJob,result)) {
        	  NrErrFiles = NrErrFiles++;
          }
      }
    }
   
 
    if (NrErrFiles>0)
    {
    	result.setResult(false);
    	result.setNrErrors(NrErrFiles);
    }
    

    return result;
  }

  private boolean ProcessFile(String filename, String wildcard, Job parentJob,Result result) {
    LogWriter log = LogWriter.getInstance();

    boolean rcode = false;
    FileObject filefolder = null;
    String realFilefoldername = environmentSubstitute(filename);
    String realwildcard = environmentSubstitute(wildcard);

    try {
      filefolder = KettleVFS.getFileObject(realFilefoldername);

      // Here gc() is explicitly called if e.g. createfile is used in the same
      // job for the same file. The problem is that after creating the file the
      // file object is not properly garbaged collected and thus the file cannot
      // be deleted anymore. This is a known problem in the JVM.

      System.gc();

      if (filefolder.exists()) {
        // the file or folder exists
	 
	    if(filefolder.getType()==FileType.FILE)
	    {
	    	// Add filename to Resultfilenames ...
	    	if(log.isDetailed()) log.logDetailed(toString(),Messages.getString("JobEntryAddResultFilenames.AddingFileToResult",filefolder.toString()));
        	ResultFile resultFile = new ResultFile(ResultFile.FILE_TYPE_GENERAL, KettleVFS.getFileObject(filefolder.toString()), parentJob.getName(), toString());
            result.getResultFiles().put(resultFile.getFile().toString(), resultFile);
	    }
	    else
	    {
	 	    FileObject list[] = filefolder.findFiles(new TextFileSelector (filefolder.toString(),realwildcard));  
	
	    	for ( int i=0; i < list.length  && !parentJob.isStopped(); i++ ) 
			{
				// Add filename to Resultfilenames ...
	    		if(log.isDetailed()) log.logDetailed(toString(),Messages.getString("JobEntryAddResultFilenames.AddingFileToResult",list[i].toString()));
	        	ResultFile resultFile = new ResultFile(ResultFile.FILE_TYPE_GENERAL, KettleVFS.getFileObject(list[i].toString()), parentJob.getName(), toString());
	            result.getResultFiles().put(resultFile.getFile().toString(), resultFile);
	        }
	    }

      } else {
        // File can not be found
    	  if(log.isBasic()) log.logBasic(toString(), Messages.getString("JobEntryAddResultFilenames.FileCanNotbeFound", realFilefoldername)); //$NON-NLS-1$
        rcode = true;
      }
    } catch (IOException e) {
      log.logError(toString(), Messages.getString("JobEntryAddResultFilenames.CouldNotProcess", realFilefoldername, e.getMessage())); //$NON-NLS-1$
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
		LogWriter log = LogWriter.getInstance();
		String file_wildcard=null,source_folder=null;
		
		public TextFileSelector(String sourcefolderin,String filewildcard) 
		 {
			 if ( !Const.isEmpty(sourcefolderin))
				 source_folder=sourcefolderin;
			
			 if ( !Const.isEmpty(filewildcard))
				 file_wildcard=filewildcard;
		 }
		 
		public boolean includeFile(FileSelectInfo info) 
		{
			boolean returncode=false;
			try
			{
				if (!info.getFile().toString().equals(source_folder))
				{
					// Pass over the Base folder itself
					String short_filename= info.getFile().getName().getBaseName();
					
					if (info.getFile().getParent().equals(info.getBaseFolder()) ||
						(!info.getFile().getParent().equals(info.getBaseFolder()) && includeSubfolders))	
					 {
						if((info.getFile().getType() == FileType.FILE && file_wildcard==null) ||
						(info.getFile().getType() == FileType.FILE && file_wildcard!=null && GetFileWildcard(short_filename,file_wildcard)))
							returncode=true;
					 }	
				}
			}
			catch (Exception e) 
			{
				log.logError(toString(), "Error while finding files ... in [" + info.getFile().toString() + "]. Exception :"+e.getMessage());
				 returncode= false;
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
  

  public void setArgumentsPrevious(boolean argFromPrevious) {
    this.argFromPrevious = argFromPrevious;
  }
  public void setDeleteAllBefore(boolean deleteallbefore) {
	    this.deleteallbefore = deleteallbefore;
	  }
	  
  
  

  public boolean evaluates() {
    return true;
  }



  public boolean isArgFromPrevious()
  {
    return argFromPrevious;
  }

  public boolean deleteAllBefore()
  {
    return deleteallbefore;
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