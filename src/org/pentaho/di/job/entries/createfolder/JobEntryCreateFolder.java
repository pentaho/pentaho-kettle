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
 
package org.pentaho.di.job.entries.createfolder;

import static org.pentaho.di.job.entry.validator.AbstractFileValidator.putVariableSpace;
import static org.pentaho.di.job.entry.validator.AndValidator.putValidators;
import static org.pentaho.di.job.entry.validator.JobEntryValidatorUtils.andValidator;
import static org.pentaho.di.job.entry.validator.JobEntryValidatorUtils.fileDoesNotExistValidator;
import static org.pentaho.di.job.entry.validator.JobEntryValidatorUtils.notNullValidator;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.pentaho.di.cluster.SlaveServer;
import org.pentaho.di.core.CheckResultInterface;
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
import org.pentaho.di.job.entries.createfile.JobEntryCreateFile;
import org.pentaho.di.job.entry.JobEntryBase;
import org.pentaho.di.job.entry.JobEntryInterface;
import org.pentaho.di.job.entry.validator.ValidatorContext;
import org.pentaho.di.repository.Repository;
import org.w3c.dom.Node;
import org.apache.commons.vfs.FileObject;
import org.apache.commons.vfs.FileType;


/**
 * This defines a 'create folder' job entry. Its main use would be to create empty folder
 * that can be used to control the flow in ETL cycles.
 * 
 * @author Sven/Samatar
 * @since 18-10-2007
 *
 */
public class JobEntryCreateFolder extends JobEntryBase implements Cloneable, JobEntryInterface
{
	private String foldername;
	private boolean failOfFolderExists;
	
	public JobEntryCreateFolder(String n)
	{
		super(n, "");
		foldername=null;
		failOfFolderExists=true;
		setID(-1L);
		setJobEntryType(JobEntryType.CREATE_FOLDER);
	}

	public JobEntryCreateFolder()
	{
		this("");
	}

	public JobEntryCreateFolder(JobEntryBase jeb)
	{
		super(jeb);
	}

    public Object clone()
    {
        JobEntryCreateFolder je = (JobEntryCreateFolder) super.clone();
        return je;
    }
    
	public String getXML()
	{
        StringBuffer retval = new StringBuffer(50);
		
		retval.append(super.getXML());		
		retval.append("      ").append(XMLHandler.addTagValue("foldername",   foldername));
		retval.append("      ").append(XMLHandler.addTagValue("fail_of_folder_exists", failOfFolderExists));
		
		return retval.toString();
	}
	
	  public void loadXML(Node entrynode, List<DatabaseMeta> databases, List<SlaveServer> slaveServers, Repository rep) throws KettleXMLException
	  {
		try
		{
			super.loadXML(entrynode, databases, slaveServers);
			foldername = XMLHandler.getTagValue(entrynode, "foldername");
			failOfFolderExists = "Y".equalsIgnoreCase(XMLHandler.getTagValue(entrynode, "fail_of_folder_exists"));
		}
		catch(KettleXMLException xe)
		{
			throw new KettleXMLException("Unable to load job entry of type 'create folder' from XML node", xe);
		}
	}

	  public void loadRep(Repository rep, long id_jobentry, List<DatabaseMeta> databases, List<SlaveServer> slaveServers) throws KettleException
	  {
		try
		{
			super.loadRep(rep, id_jobentry, databases, slaveServers);
			foldername = rep.getJobEntryAttributeString(id_jobentry, "foldername");
			failOfFolderExists = rep.getJobEntryAttributeBoolean(id_jobentry, "fail_of_folder_exists");
		}
		catch(KettleException dbe)
		{
			throw new KettleException("Unable to load job entry of type 'create Folder' from the repository for id_jobentry="+id_jobentry, dbe);
		}
	}
	
	public void saveRep(Repository rep, long id_job)
		throws KettleException
	{
		try
		{
			super.saveRep(rep, id_job);
			
			rep.saveJobEntryAttribute(id_job, getID(), "foldername", foldername);
            rep.saveJobEntryAttribute(id_job, getID(), "fail_of_folder_exists", failOfFolderExists);
		}
		catch(KettleDatabaseException dbe)
		{
			throw new KettleException("Unable to save job entry of type 'create Folder' to the repository for id_job="+id_job, dbe);
		}
	}

	public void setFoldername(String foldername)
	{
		this.foldername = foldername;
	}
	
	public String getFoldername()
	{
		return foldername;
	}
    
    public String getRealFoldername()
    {
        return environmentSubstitute(getFoldername());
    }
	
	public Result execute(Result previousResult, int nr, Repository rep, Job parentJob)
	{
		LogWriter log = LogWriter.getInstance();
		Result result = previousResult;
		result.setResult( false );
		
	
		if (foldername!=null)
		{
            String realFoldername = getRealFoldername();
            FileObject FolderObject = null;
			try {
				FolderObject = KettleVFS.getFileObject(realFoldername, this);

				if ( FolderObject.exists() )
				{
					boolean isFolder=false;
					
					//Check if it's a folder
					if(FolderObject.getType() == FileType.FOLDER)  isFolder=true;
					
						if ( isFailOfFolderExists() )
						{
							// Folder exists and fail flag is on.
						    result.setResult( false );
						    if(isFolder)
						    	log.logError(toString(), "Folder ["+realFoldername+"] exists, failing.");
						    else
						    	log.logError(toString(), "File ["+realFoldername+"] exists, failing.");
						}
						else
						{
							// Folder already exists, no reason to try to create it
						    result.setResult( true );
						    if(log.isDetailed())
						    	log.logDetailed(toString(), "Folder ["+realFoldername+"] already exists, not recreating.");
						}
				
					
				}
				else
				{
					//  No Folder yet, create an empty Folder.
					FolderObject.createFolder();
					if(log.isDetailed())
						log.logDetailed(toString(), "Folder ["+realFoldername+"] created!");
					result.setResult( true );
				}
			} catch (IOException e) {
				log.logError(toString(), "Could not create Folder ["+realFoldername+"], exception: " + e.getMessage());
				result.setResult( false );
				result.setNrErrors(1);					
			}
            finally {
            	if ( FolderObject != null )
            	{
            		try  {
            		     FolderObject.close();
            		}
            		catch ( IOException ex ) {};
            	}
            }			
		}
		else
		{			
			log.logError(toString(), "No Foldername is defined.");
		}
		
		return result;
	}

	public boolean evaluates()
	{
		return true;
	}
    


	public boolean isFailOfFolderExists() {
		return failOfFolderExists;
	}

	public void setFailOfFolderExists(boolean failIfFolderExists) {
		this.failOfFolderExists = failIfFolderExists;
	}
	
	public static void main(String[] args)
	  {
	    List<CheckResultInterface> remarks = new ArrayList<CheckResultInterface>();
	    new JobEntryCreateFile().check(remarks, null);
	    System.out.printf("Remarks: %s\n", remarks);
	  }

	  public void check(List<CheckResultInterface> remarks, JobMeta jobMeta)
	  {
	    ValidatorContext ctx = new ValidatorContext();
	    putVariableSpace(ctx, getVariables());
	    putValidators(ctx, notNullValidator(), fileDoesNotExistValidator());
	    andValidator().validate(this, "filename", remarks, ctx); //$NON-NLS-1$
	  }
}