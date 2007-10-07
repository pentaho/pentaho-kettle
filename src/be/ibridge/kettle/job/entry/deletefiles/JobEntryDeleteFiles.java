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

package be.ibridge.kettle.job.entry.deletefiles;



import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.swt.widgets.Shell;
import org.w3c.dom.Node;

import be.ibridge.kettle.core.LogWriter;
import be.ibridge.kettle.core.Result;
import be.ibridge.kettle.core.Row;
import be.ibridge.kettle.core.Const;
import be.ibridge.kettle.core.XMLHandler;
import be.ibridge.kettle.core.exception.KettleDatabaseException;
import be.ibridge.kettle.core.exception.KettleException;
import be.ibridge.kettle.core.exception.KettleXMLException;
import be.ibridge.kettle.core.util.StringUtil;
import be.ibridge.kettle.job.Job;
import be.ibridge.kettle.job.JobMeta;
import be.ibridge.kettle.job.entry.JobEntryBase;
import be.ibridge.kettle.job.entry.JobEntryDialogInterface;
import be.ibridge.kettle.job.entry.JobEntryInterface;
import be.ibridge.kettle.repository.Repository;
import be.ibridge.kettle.core.vfs.KettleVFS;
import org.apache.commons.vfs.FileObject;
import org.apache.commons.vfs.FileType;
import org.apache.commons.vfs.FileSelector;
import org.apache.commons.vfs.FileSelectInfo;


/**
 * This defines a 'delete files' job entry.
 *
 * @author Samatar Hassan
 * @since 06-05-2007
 */
public class JobEntryDeleteFiles extends JobEntryBase implements Cloneable, JobEntryInterface
{

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
    setType(JobEntryInterface.TYPE_JOBENTRY_DELETE_FILES);
  }

  public JobEntryDeleteFiles() {
    this(""); //$NON-NLS-1$
  }

  public JobEntryDeleteFiles(JobEntryBase jeb) {
    super(jeb);
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

  public void loadXML(Node entrynode, ArrayList databases, Repository rep)	throws KettleXMLException
	{
    try {
      super.loadXML(entrynode, databases);
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
      throw new KettleXMLException(Messages.getString("JobEntryDeleteFiles.UnableToLoadFromXml"), xe); //$NON-NLS-1$
    }
  }

  public void loadRep(Repository rep, long id_jobentry, ArrayList databases)
	throws KettleException
{
	try
	{
      super.loadRep(rep, id_jobentry, databases);
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
      throw new KettleException(Messages.getString(
          "JobEntryDeleteFiles.UnableToLoadFromRepo", String.valueOf(id_jobentry)), dbe); //$NON-NLS-1$
    }
  }

  public void saveRep(Repository rep, long id_job) throws KettleException {
    try {
      super.saveRep(rep, id_job);

      rep.saveJobEntryAttribute(id_job, getID(), "arg_from_previous", argFromPrevious); //$NON-NLS-1$
      rep.saveJobEntryAttribute(id_job, getID(), "include_subfolders", includeSubfolders); //$NON-NLS-1$

      // save the arguments...
      if (arguments != null) {
        for (int i = 0; i < arguments.length; i++) {
          rep.saveJobEntryAttribute(id_job, getID(), i, "name", arguments[i]); //$NON-NLS-1$
          rep.saveJobEntryAttribute(id_job, getID(), i, "filemask", filemasks[i]); //$NON-NLS-1$
        }
      }
    } catch (KettleDatabaseException dbe) {
      throw new KettleException(
          Messages.getString("JobEntryDeleteFiles.UnableToSaveToRepo", String.valueOf(id_job)), dbe); //$NON-NLS-1$
    }
  }

  public Result execute(Result result, int nr, Repository rep, Job parentJob) throws KettleException {
    LogWriter log = LogWriter.getInstance();

    List rows = result.getRows();
    Row resultRow = null;

    int NrErrFiles = 0;

    String args[] = arguments;
    String fmasks[] = filemasks;
    result.setResult(true);


    if (argFromPrevious) {
      log.logDetailed(toString(), Messages.getString(
          "JobEntryDeleteFiles.FoundPreviousRows", String.valueOf((rows != null ? rows.size() : 0)))); //$NON-NLS-1$
    }

    if (argFromPrevious && rows != null) // Copy the input row to the (command line) arguments
    {

      for (int iteration = 0; iteration < rows.size(); iteration++) {
    	  resultRow = (Row) rows.get(iteration);
        args = new String[resultRow.size()];
        fmasks = new String[resultRow.size()];


        args[iteration] = resultRow.getValue(0).getString();
        fmasks[iteration] = resultRow.getValue(1).getString();        

          // ok we can process this file/folder
          log.logDetailed(toString(), Messages.getString(
              "JobEntryDeleteFiles.ProcessingRow", args[iteration], fmasks[iteration])); //$NON-NLS-1$

          if (!ProcessFile(args[iteration], fmasks[iteration])) {
        	  NrErrFiles = NrErrFiles++;
          }
       
      }
    } else if (arguments != null) {

      for (int i = 0; i < arguments.length; i++) {
        
          // ok we can process this file/folder
          log.logDetailed(toString(), Messages.getString(
              "JobEntryDeleteFiles.ProcessingArg", arguments[i], filemasks[i])); //$NON-NLS-1$
          if (!ProcessFile(arguments[i], filemasks[i])) {
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

  private boolean ProcessFile(String filename, String wildcard) {
    LogWriter log = LogWriter.getInstance();

    boolean rcode = false;
    FileObject filefolder = null;
    String realFilefoldername = StringUtil.environmentSubstitute(filename);
    String realwilcard = StringUtil.environmentSubstitute(wildcard);

    try {
      filefolder = KettleVFS.getFileObject(realFilefoldername);

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
            log.logDetailed(toString(), Messages.getString("JobEntryDeleteFiles.ProcessingFolder", realFilefoldername)); //$NON-NLS-1$
          // Delete Files
          
          int Nr = filefolder.delete(new TextFileSelector(filefolder.toString(),realwilcard));

          if (log.isDetailed())
            log.logDetailed(toString(), Messages.getString("JobEntryDeleteFiles.TotalDeleted", String.valueOf(Nr))); //$NON-NLS-1$
          rcode = true;
        } else {
          // It's a file
          log.logDetailed(toString(), Messages.getString("JobEntryDeleteFiles.ProcessingFile", realFilefoldername)); //$NON-NLS-1$
          boolean deleted = filefolder.delete();
          if (!deleted) {
            log.logError(toString(), Messages.getString("JobEntryDeleteFiles.CouldNotDeleteFile", realFilefoldername)); //$NON-NLS-1$
          } else {
            log.logBasic(toString(), Messages.getString("JobEntryDeleteFiles.FileDeleted", filename)); //$NON-NLS-1$
            rcode = true;
          }
        }
      } else {
        // File already deleted, no reason to try to delete it
        log.logBasic(toString(), Messages.getString("JobEntryDeleteFiles.FileAlreadyDeleted", realFilefoldername)); //$NON-NLS-1$
        rcode = true;
      }
    } catch (IOException e) {
      log.logError(toString(), Messages.getString(
          "JobEntryDeleteFiles.CouldNotProcess", realFilefoldername, e.getMessage())); //$NON-NLS-1$
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
							if (log.isDetailed())
							{ 
								log.logDetailed(toString(), Messages.getString("JobEntryDeleteFiles.DeletingFile",info.getFile().toString())); //$NON-NLS-1$
							}
							returncode= true; 				
							 
						 }
					 }
					 else
					 {
						// In the Base Folder...
						 
						 if ((info.getFile().getType() == FileType.FILE) && GetFileWildcard(short_filename,file_wildcard))
						 {
							if (log.isDetailed())
							{ 
								log.logDetailed(toString(), Messages.getString("JobEntryDeleteFiles.DeletingFile",info.getFile().toString())); //$NON-NLS-1$
							}
							returncode= true; 				
							 
						 }
						
					 }
					
				}
				
			}
			catch (Exception e) 
			{
				

				log.logError(Messages.getString("JobDeleteFiles.Error.Exception.DeleteProcessError") , Messages.getString("JobDeleteFiles.Error.Exception.DeleteProcess", 
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
  
	public JobEntryDialogInterface getDialog(Shell shell,JobEntryInterface jei,JobMeta jobMeta,String jobName,Repository rep) 
	{
		return new JobEntryDeleteFilesDialog(shell,this,jobMeta);
	}

}