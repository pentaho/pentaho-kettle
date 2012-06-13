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

package org.pentaho.di.job.entries.folderisempty;

import static org.pentaho.di.job.entry.validator.AndValidator.putValidators;
import static org.pentaho.di.job.entry.validator.JobEntryValidatorUtils.andValidator;
import static org.pentaho.di.job.entry.validator.JobEntryValidatorUtils.notBlankValidator;

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
import org.w3c.dom.Node;


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
	private static Class<?> PKG = JobEntryFolderIsEmpty.class; // for i18n purposes, needed by Translator2!!   $NON-NLS-1$

	private String foldername;
	private int filescount;
	private int folderscount;
	private boolean includeSubfolders;
	private boolean specifywildcard;
	private String wildcard;
	private Pattern pattern;
	
	public JobEntryFolderIsEmpty(String n)
	{
		super(n, "");
		foldername=null;
		wildcard=null;
		includeSubfolders=false;
		specifywildcard=false;
		setID(-1L);
	}

	public JobEntryFolderIsEmpty()
	{
		this("");
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

	public void loadRep(Repository rep, ObjectId id_jobentry, List<DatabaseMeta> databases, List<SlaveServer> slaveServers) throws KettleException
	{
		try
		{
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
	
	public void saveRep(Repository rep, ObjectId id_job) throws KettleException
	{
		try
		{
			rep.saveJobEntryAttribute(id_job, getObjectId(), "foldername", foldername);
			rep.saveJobEntryAttribute(id_job, getObjectId(), "include_subfolders", includeSubfolders);
			rep.saveJobEntryAttribute(id_job, getObjectId(), "specify_wildcard", specifywildcard);
			rep.saveJobEntryAttribute(id_job, getObjectId(), "wildcard", wildcard);
		}
		catch(KettleDatabaseException dbe)
		{
			throw new KettleException("Unable to save job entry of type 'create Folder' to the repository for id_job="+id_job, dbe);
		}
	}
	
	public void setSpecifyWildcard(boolean specifywildcard)
	{
		this.specifywildcard=specifywildcard;
	}

	public boolean isSpecifyWildcard()
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
	public Result execute(Result previousResult, int nr)
	{
		Result result = previousResult;
		result.setResult( false );
		result.setNrErrors(1);
		
		filescount=0;
		folderscount=0;
		pattern = null;
		
		if (!Const.isEmpty(getWildcard()))  pattern = Pattern.compile(getRealWildcard());
		
		if (foldername!=null)
		{
            String realFoldername = getRealFoldername();
            FileObject folderObject = null;
			try {
				folderObject = KettleVFS.getFileObject(realFoldername, this);

				if ( folderObject.exists() )
				{
					//Check if it's a folder
					if(folderObject.getType() == FileType.FOLDER) 
					{
						// File provided is a folder, so we can process ...
						folderObject.findFiles(new TextFileSelector(folderObject.toString()));
						if(log.isBasic())	log.logBasic("Total files", "We found : "+filescount + " file(s)");
						if(filescount==0)
						{
							result.setResult(true);
		          result.setNrLinesInput(folderscount);
		          result.setNrErrors(0);
						}
					}
					else
					{
						// Not a folder, fail
						log.logError("[" + realFoldername+"] is not a folder, failing.");
					}	
				}
				else
				{
					//  No Folder found	
					if(log.isBasic()) logBasic("we can not find ["+realFoldername+"] !");
				}
			} catch (Exception e) {
				logError("Error checking folder ["+realFoldername+"]", e);
				result.setResult( false );
				result.setNrErrors(1);					
			}
            finally {
            	if ( folderObject != null )
            	{
            		try  {
            		     folderObject.close();
            		     folderObject=null;
            		}
            		catch ( IOException ex ) {};
            	}
            }			
		}
		else
		{			
			logError("No Foldername is defined.");
		}
		
		return result;
	}
	
	private class TextFileSelector implements FileSelector 
	{
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
							if((isSpecifyWildcard() && GetFileWildcard(info.getFile().getName().getBaseName())) || !isSpecifyWildcard())
							{
								if(log.isDetailed()) log.logDetailed("We found file : " + info.getFile().toString());
								filescount++; 
							}
						 }
						else
						{
							// We are not in the base Folder...ONLY if Use sub folders
							// We are in the Base folder							
							if(isIncludeSubFolders())
							{
								if((isSpecifyWildcard() && GetFileWildcard(info.getFile().getName().getBaseName())) || !isSpecifyWildcard())
								{
									if(log.isDetailed()) log.logDetailed("We found file : " + info.getFile().toString());
									filescount++; 
								}
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
				log.logError(BaseMessages.getString(PKG, "JobFolderIsEmpty.Error") , 
						BaseMessages.getString(PKG, "JobFolderIsEmpty.Error.Exception", 
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
						file_name=null;
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
	private boolean GetFileWildcard(String selectedfile)
	{
		boolean getIt=true;
	
		// First see if the file matches the regular expression!
		if (pattern!=null)
		{
			Matcher matcher = pattern.matcher(selectedfile);
			getIt = matcher.matches();
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