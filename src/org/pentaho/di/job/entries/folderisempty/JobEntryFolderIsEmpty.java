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
 
package org.pentaho.di.job.entries.folderisempty;

import static org.pentaho.di.job.entry.validator.AndValidator.putValidators;
import static org.pentaho.di.job.entry.validator.JobEntryValidatorUtils.andValidator;
import static org.pentaho.di.job.entry.validator.JobEntryValidatorUtils.notBlankValidator;

import java.io.IOException;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.w3c.dom.Node;

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
import org.apache.commons.vfs.FileObject;
import org.apache.commons.vfs.FileType;
import org.apache.commons.vfs.FileSelector;
import org.apache.commons.vfs.FileSelectInfo;


/**
 * This defines a 'create folder' job entry. Its main use would be to create empty folder
 * that can be used to control the flow in ETL cycles.
 * 
 * @author Sven/Samatar
 * @since 18-10-2007
 *
 */
public class JobEntryFolderIsEmpty extends JobEntryBase implements Cloneable, JobEntryInterface
{
	private String foldername;
	private int filescount;
	private int folderscount;
	private boolean includeSubfolders;
	private boolean specifywildcard;
	private String wildcard;
	
	public JobEntryFolderIsEmpty(String n)
	{
		super(n, "");
		foldername=null;
		wildcard=null;
		includeSubfolders=false;
		specifywildcard=false;
		setID(-1L);
		setJobEntryType(JobEntryType.FOLDER_IS_EMPTY);
	}

	public JobEntryFolderIsEmpty()
	{
		this("");
	}

	public JobEntryFolderIsEmpty(JobEntryBase jeb)
	{
		super(jeb);
	}

    public Object clone()
    {
        JobEntryFolderIsEmpty je = (JobEntryFolderIsEmpty) super.clone();
        return je;
    }
    
	public String getXML()
	{
        StringBuffer retval = new StringBuffer(50);
		
		retval.append(super.getXML());		
		retval.append("      ").append(XMLHandler.addTagValue("foldername",   foldername));
		retval.append("      ").append(XMLHandler.addTagValue("include_subfolders", includeSubfolders));
		retval.append("      ").append(XMLHandler.addTagValue("specify_wildcard", specifywildcard));
		retval.append("      ").append(XMLHandler.addTagValue("wildcard",   wildcard));
		
		
		return retval.toString();
	}
	
	public void loadXML(Node entrynode, List<DatabaseMeta> databases, List<SlaveServer> slaveServers, Repository rep) throws KettleXMLException
	{
		try
		{
			super.loadXML(entrynode, databases, slaveServers);
			foldername = XMLHandler.getTagValue(entrynode, "foldername");
			includeSubfolders = "Y".equalsIgnoreCase(XMLHandler.getTagValue(entrynode, "include_subfolders"));
			specifywildcard = "Y".equalsIgnoreCase(XMLHandler.getTagValue(entrynode, "specify_wildcard"));
			wildcard = XMLHandler.getTagValue(entrynode, "wildcard");
			
			
			
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
			includeSubfolders = rep.getJobEntryAttributeBoolean(id_jobentry, "include_subfolders"); 
			specifywildcard = rep.getJobEntryAttributeBoolean(id_jobentry, "specify_wildcard");  
			wildcard = rep.getJobEntryAttributeString(id_jobentry, "wildcard");
			
			
			 
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
			rep.saveJobEntryAttribute(id_job, getID(), "include_subfolders", includeSubfolders);
			rep.saveJobEntryAttribute(id_job, getID(), "specify_wildcard", specifywildcard);
			rep.saveJobEntryAttribute(id_job, getID(), "wildcard", wildcard);
			
	
		}
		catch(KettleDatabaseException dbe)
		{
			throw new KettleException("Unable to save job entry of type 'create Folder' to the repository for id_job="+id_job, dbe);
		}
	}
	
	public void setSpecifyWilcard(boolean specifywildcard)
	{
		this.specifywildcard=specifywildcard;
	}

	public boolean isSpecifyWilcard()
	{
		return specifywildcard;
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
    
    public String getWildcard()
    {
    	return wildcard;
    }
    
    public String getRealWildcard()
    {
    	return environmentSubstitute(getWildcard());
    }
    public void setWildcard(String wildcard)
    {
    	this.wildcard=wildcard;
    }
    public boolean isIncludeSubFolders()
    {
    	return includeSubfolders;
    }
	
    public void setIncludeSubFolders(boolean includeSubfolders)
    {
    	this.includeSubfolders=includeSubfolders;
    }
	public Result execute(Result previousResult, int nr, Repository rep, Job parentJob)
	{
		LogWriter log = LogWriter.getInstance();
		Result result = previousResult;
		result.setResult( false );
		
		filescount=0;
		folderscount=0;
		
		if (foldername!=null)
		{
            String realFoldername = getRealFoldername();
            FileObject FolderObject = null;
			try {
				FolderObject = KettleVFS.getFileObject(realFoldername);

				if ( FolderObject.exists() )
				{
					//Check if it's a folder
					if(FolderObject.getType() == FileType.FOLDER) 
					{
						// File provided is a folder, so we can process ...
						FolderObject.findFiles(new TextFileSelector(FolderObject.toString()));
						
						if(log.isBasic())
							log.logBasic("Total files", "We found : "+filescount + " file(s)");
						
						if(filescount==0) result.setResult(true);
						
						
					}
					else
					{
						// Not a folder, fail
						log.logError("Found files", "[" + realFoldername+"] is not a folder, failing.");
					}
						
				}
				else
				{
					//  No Folder found	
					if(log.isBasic())
						log.logBasic(toString(), "we can not find ["+realFoldername+"] !");
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
	
	private class TextFileSelector implements FileSelector 
	{
		LogWriter log = LogWriter.getInstance();
		String root_folder=null;
		
		public TextFileSelector(String rootfolder) 
		 {
			
			 if (rootfolder!=null)
			 {
				 root_folder=rootfolder;
			 }
		 }
		 
		public boolean includeFile(FileSelectInfo info) 
		{
			boolean returncode=false;
			FileObject file_name=null;
			
			try
			{
				if (!info.getFile().toString().equals(root_folder))
				{
					// Pass over the Base folder itself
					if ((info.getFile().getType() == FileType.FILE))
					{
						
						if (info.getFile().getParent().equals(info.getBaseFolder()))
						 {
							// We are in the Base folder
							if((isSpecifyWilcard() && GetFileWildcard(info.getFile().toString()) || !isSpecifyWilcard()))
							{
								if(log.isDetailed()) log.logDetailed("Found files", "We found file : " + info.getFile().toString());
								filescount++; 
							}
						 }
						else
						{
							// We are not in the base Folder...ONLY if Use sub folders
							// We are in the Base folder
							if((isSpecifyWilcard() && GetFileWildcard(info.getFile().toString()) || !isSpecifyWilcard())
									&& (isIncludeSubFolders()))
							{
								if(log.isDetailed()) log.logDetailed("Found files", "We found file : " + info.getFile().toString());
								filescount++; 
							}
						}
					}
					else
					{
						folderscount++;
					}
				}
				return true;
								
			}
			catch (Exception e) 
			{
				log.logError(Messages.getString("JobFolderIsEmpty.Error") , Messages.getString("JobFolderIsEmpty.Error.Exception", 
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
			return true;//isIncludeSubFolders();
		}
	}
	/**********************************************************
	 * 
	 * @param selectedfile
	 * @param wildcard
	 * @return True if the selectedfile matches the wildcard
	 **********************************************************/
	private boolean GetFileWildcard(String selectedfile)
	{
		Pattern pattern = null;
		boolean getIt=true;
	
        if (!Const.isEmpty(getWildcard()))
        {
        	 pattern = Pattern.compile(getRealWildcard());
			// First see if the file matches the regular expression!
			if (pattern!=null)
			{
				Matcher matcher = pattern.matcher(selectedfile);
				getIt = matcher.matches();
			}
        }
		
		return getIt;
	}

	public boolean evaluates()
	{
		return true;
	}
    
	
	  @Override
	  public void check(List<CheckResultInterface> remarks, JobMeta jobMeta) {
	    andValidator().validate(this, "filename", remarks, putValidators(notBlankValidator())); //$NON-NLS-1$
	  }
}