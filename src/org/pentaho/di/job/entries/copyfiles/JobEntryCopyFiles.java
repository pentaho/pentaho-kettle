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
 
package org.pentaho.di.job.entries.copyfiles;
import static org.pentaho.di.job.entry.validator.AbstractFileValidator.putVariableSpace;
import static org.pentaho.di.job.entry.validator.AndValidator.putValidators;
import static org.pentaho.di.job.entry.validator.JobEntryValidatorUtils.andValidator;
import static org.pentaho.di.job.entry.validator.JobEntryValidatorUtils.fileExistsValidator;
import static org.pentaho.di.job.entry.validator.JobEntryValidatorUtils.notNullValidator;

import java.io.IOException;
import java.util.HashSet;
import java.util.Iterator;
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
import org.pentaho.di.core.ResultFile;
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
import org.w3c.dom.Node;



/**
 * This defines a 'copy files' job entry.
 * 
 * @author Samatar Hassan
 * @since 06-05-2007
 */
public class JobEntryCopyFiles extends JobEntryBase implements Cloneable, JobEntryInterface
{
    private static Class<?> PKG = JobEntryCopyFiles.class; // for i18n purposes, needed by Translator2!!   $NON-NLS-1$

	public boolean copy_empty_folders;
	public  boolean arg_from_previous;
	public  boolean overwrite_files;
	public  boolean include_subfolders;
	public boolean add_result_filesname;
	public boolean remove_source_files;
	public boolean destination_is_a_file;
	public boolean create_destination_folder;
	public  String  source_filefolder[];
	public  String  destination_filefolder[];
	public  String  wildcard[];
	HashSet<String> list_files_remove = new HashSet<String>();
	HashSet<String> list_add_result = new HashSet<String>();
	int NbrFail=0;
	
	public JobEntryCopyFiles(String n)
	{
		super(n, ""); //$NON-NLS-1$
		copy_empty_folders=true;
		arg_from_previous=false;
		source_filefolder=null;
		remove_source_files=false;
		destination_filefolder=null;
		wildcard=null;
		overwrite_files=false;
		include_subfolders=false;
		add_result_filesname=false;
		destination_is_a_file=false;
		create_destination_folder=false;
		setID(-1L);
	}

	public JobEntryCopyFiles()
	{
		this(""); //$NON-NLS-1$
	}

	public Object clone()
	{
		JobEntryCopyFiles je = (JobEntryCopyFiles) super.clone();
		return je;
	}
    
	public String getXML()
	{
		StringBuffer retval = new StringBuffer(300);
		
		retval.append(super.getXML());		
		retval.append("      ").append(XMLHandler.addTagValue("copy_empty_folders",      copy_empty_folders)); //$NON-NLS-1$ //$NON-NLS-2$
		retval.append("      ").append(XMLHandler.addTagValue("arg_from_previous",  arg_from_previous)); //$NON-NLS-1$ //$NON-NLS-2$
		retval.append("      ").append(XMLHandler.addTagValue("overwrite_files",      overwrite_files)); //$NON-NLS-1$ //$NON-NLS-2$
		retval.append("      ").append(XMLHandler.addTagValue("include_subfolders", include_subfolders)); //$NON-NLS-1$ //$NON-NLS-2$
		retval.append("      ").append(XMLHandler.addTagValue("remove_source_files", remove_source_files)); //$NON-NLS-1$ //$NON-NLS-2$
		retval.append("      ").append(XMLHandler.addTagValue("add_result_filesname", add_result_filesname)); //$NON-NLS-1$ //$NON-NLS-2$
		retval.append("      ").append(XMLHandler.addTagValue("destination_is_a_file", destination_is_a_file)); //$NON-NLS-1$ //$NON-NLS-2$
		retval.append("      ").append(XMLHandler.addTagValue("create_destination_folder", create_destination_folder)); //$NON-NLS-1$ //$NON-NLS-2$
		
		retval.append("      <fields>").append(Const.CR); //$NON-NLS-1$
		if (source_filefolder!=null)
		{
			for (int i=0;i<source_filefolder.length;i++)
			{
				retval.append("        <field>").append(Const.CR); //$NON-NLS-1$
				retval.append("          ").append(XMLHandler.addTagValue("source_filefolder",     source_filefolder[i])); //$NON-NLS-1$ //$NON-NLS-2$
				retval.append("          ").append(XMLHandler.addTagValue("destination_filefolder",     destination_filefolder[i])); //$NON-NLS-1$ //$NON-NLS-2$
				retval.append("          ").append(XMLHandler.addTagValue("wildcard", wildcard[i])); //$NON-NLS-1$ //$NON-NLS-2$
				retval.append("        </field>").append(Const.CR); //$NON-NLS-1$
			}
		}
		retval.append("      </fields>").append(Const.CR);
		
		return retval.toString();
	}
	
	
	 public void loadXML(Node entrynode, List<DatabaseMeta> databases, List<SlaveServer> slaveServers, Repository rep) throws KettleXMLException
	  {
		try
		{
			super.loadXML(entrynode, databases, slaveServers);
			copy_empty_folders      = "Y".equalsIgnoreCase(XMLHandler.getTagValue(entrynode, "copy_empty_folders")); //$NON-NLS-1$ //$NON-NLS-2$
			arg_from_previous   = "Y".equalsIgnoreCase(XMLHandler.getTagValue(entrynode, "arg_from_previous") ); //$NON-NLS-1$ //$NON-NLS-2$
			overwrite_files      = "Y".equalsIgnoreCase(XMLHandler.getTagValue(entrynode, "overwrite_files") ); //$NON-NLS-1$ //$NON-NLS-2$
			include_subfolders = "Y".equalsIgnoreCase(XMLHandler.getTagValue(entrynode, "include_subfolders") ); //$NON-NLS-1$ //$NON-NLS-2$
			remove_source_files = "Y".equalsIgnoreCase(XMLHandler.getTagValue(entrynode, "remove_source_files") ); //$NON-NLS-1$ //$NON-NLS-2$
			add_result_filesname = "Y".equalsIgnoreCase(XMLHandler.getTagValue(entrynode, "add_result_filesname") ); //$NON-NLS-1$ //$NON-NLS-2$
			destination_is_a_file = "Y".equalsIgnoreCase(XMLHandler.getTagValue(entrynode, "destination_is_a_file") ); //$NON-NLS-1$ //$NON-NLS-2$
			create_destination_folder = "Y".equalsIgnoreCase(XMLHandler.getTagValue(entrynode, "create_destination_folder") ); //$NON-NLS-1$ //$NON-NLS-2$
					
			Node fields = XMLHandler.getSubNode(entrynode, "fields"); //$NON-NLS-1$
			
			// How many field arguments?
			int nrFields = XMLHandler.countNodes(fields, "field");	//$NON-NLS-1$ 
			source_filefolder = new String[nrFields];
			destination_filefolder = new String[nrFields];
			wildcard = new String[nrFields];
			
			// Read them all...
			for (int i = 0; i < nrFields; i++)
			{
				Node fnode = XMLHandler.getSubNodeByNr(fields, "field", i);//$NON-NLS-1$ 
				
				source_filefolder[i] = XMLHandler.getTagValue(fnode, "source_filefolder");//$NON-NLS-1$ 
				destination_filefolder[i] = XMLHandler.getTagValue(fnode, "destination_filefolder");//$NON-NLS-1$ 
				wildcard[i] = XMLHandler.getTagValue(fnode, "wildcard");//$NON-NLS-1$ 
			}
		}
	
		catch(KettleXMLException xe)
		{
			
			throw new KettleXMLException(BaseMessages.getString(PKG, "JobCopyFiles.Error.Exception.UnableLoadXML"), xe);
		}
	}

	 public void loadRep(Repository rep, ObjectId id_jobentry, List<DatabaseMeta> databases, List<SlaveServer> slaveServers) throws KettleException
	  {
		try
		{
      copy_empty_folders      = rep.getJobEntryAttributeBoolean(id_jobentry, "copy_empty_folders");//$NON-NLS-1$ 
      arg_from_previous   = rep.getJobEntryAttributeBoolean(id_jobentry, "arg_from_previous");//$NON-NLS-1$ 
      overwrite_files      = rep.getJobEntryAttributeBoolean(id_jobentry, "overwrite_files");//$NON-NLS-1$ 
      include_subfolders = rep.getJobEntryAttributeBoolean(id_jobentry, "include_subfolders");//$NON-NLS-1$ 
      remove_source_files = rep.getJobEntryAttributeBoolean(id_jobentry, "remove_source_files");//$NON-NLS-1$ 
			
			add_result_filesname = rep.getJobEntryAttributeBoolean(id_jobentry, "add_result_filesname");//$NON-NLS-1$ 
			destination_is_a_file = rep.getJobEntryAttributeBoolean(id_jobentry, "destination_is_a_file");//$NON-NLS-1$ 
			create_destination_folder = rep.getJobEntryAttributeBoolean(id_jobentry, "create_destination_folder");//$NON-NLS-1$ 
				
			// How many arguments?
			int argnr = rep.countNrJobEntryAttributes(id_jobentry, "source_filefolder");//$NON-NLS-1$ 
			source_filefolder = new String[argnr];
			destination_filefolder = new String[argnr];
			wildcard = new String[argnr];
			
			// Read them all...
			for (int a=0;a<argnr;a++) 
			{
				source_filefolder[a]= rep.getJobEntryAttributeString(id_jobentry, a, "source_filefolder");//$NON-NLS-1$ 
				destination_filefolder[a]= rep.getJobEntryAttributeString(id_jobentry, a, "destination_filefolder");//$NON-NLS-1$ 
				wildcard[a]= rep.getJobEntryAttributeString(id_jobentry, a, "wildcard");//$NON-NLS-1$ 
			}
		}
		catch(KettleException dbe)
		{
			
			throw new KettleException(BaseMessages.getString(PKG, "JobCopyFiles.Error.Exception.UnableLoadRep")+id_jobentry, dbe);
		}
	}
	
	public void saveRep(Repository rep, ObjectId id_job) throws KettleException
	{
		try
		{
			rep.saveJobEntryAttribute(id_job, getObjectId(), "copy_empty_folders",      copy_empty_folders);
			rep.saveJobEntryAttribute(id_job, getObjectId(), "arg_from_previous",  arg_from_previous);
			rep.saveJobEntryAttribute(id_job, getObjectId(), "overwrite_files",      overwrite_files);
			rep.saveJobEntryAttribute(id_job, getObjectId(), "include_subfolders", include_subfolders);
			rep.saveJobEntryAttribute(id_job, getObjectId(), "remove_source_files", remove_source_files);
			rep.saveJobEntryAttribute(id_job, getObjectId(), "add_result_filesname", add_result_filesname);
			rep.saveJobEntryAttribute(id_job, getObjectId(), "destination_is_a_file", destination_is_a_file);
			rep.saveJobEntryAttribute(id_job, getObjectId(), "create_destination_folder", create_destination_folder);
			
			// save the arguments...
			if (source_filefolder!=null)
			{
				for (int i=0;i<source_filefolder.length;i++) 
				{
					rep.saveJobEntryAttribute(id_job, getObjectId(), i, "source_filefolder",     source_filefolder[i]);
					rep.saveJobEntryAttribute(id_job, getObjectId(), i, "destination_filefolder",     destination_filefolder[i]);
					rep.saveJobEntryAttribute(id_job, getObjectId(), i, "wildcard", wildcard[i]);
				}
			}
		}
		catch(KettleDatabaseException dbe)
		{
			
			throw new KettleException(BaseMessages.getString(PKG, "JobCopyFiles.Error.Exception.UnableSaveRep")+id_job, dbe);
		}
	}

	public Result execute(Result previousResult, int nr) throws KettleException 
	{
		LogWriter log = LogWriter.getInstance();
		Result result = previousResult;

	    List<RowMetaAndData> rows = result.getRows();
	    RowMetaAndData resultRow = null;
		
		int NbrFail=0;    
		
		NbrFail=0;
	
		if(log.isBasic()) logBasic(BaseMessages.getString(PKG, "JobCopyFiles.Log.Starting"));
		
		// Get source and destination files, also wildcard
		String vsourcefilefolder[] = source_filefolder;
		String vdestinationfilefolder[] = destination_filefolder;
		String vwildcard[] = wildcard;
		
		result.setResult( false );
		result.setNrErrors(1);
		
		if (arg_from_previous)
		{
			if(log.isDetailed())	
				logDetailed(BaseMessages.getString(PKG, "JobCopyFiles.Log.ArgFromPrevious.Found",(rows!=null?rows.size():0)+ ""));
		}

		if (arg_from_previous && rows!=null) // Copy the input row to the (command line) arguments
		{
			for (int iteration=0;iteration<rows.size() && !parentJob.isStopped();iteration++) 
			{
				resultRow = rows.get(iteration);
				
				// Get source and destination file names, also wildcard
				String vsourcefilefolder_previous = resultRow.getString(0,null);
				String vdestinationfilefolder_previous = resultRow.getString(1,null);
				String vwildcard_previous = resultRow.getString(2,null);
				
				if(!Const.isEmpty(vsourcefilefolder_previous) &&  !Const.isEmpty(vdestinationfilefolder_previous))
				{
					if(log.isDetailed()) logDetailed(BaseMessages.getString(PKG, "JobCopyFiles.Log.ProcessingRow",vsourcefilefolder_previous, vdestinationfilefolder_previous, vwildcard_previous));

					if(! ProcessFileFolder(vsourcefilefolder_previous,vdestinationfilefolder_previous,vwildcard_previous,parentJob,result))
					{
						// The copy process fail
						NbrFail++;
					}
				}
				else
				{
					 if(log.isDetailed())
						 logDetailed(BaseMessages.getString(PKG, "JobCopyFiles.Log.IgnoringRow",vsourcefilefolder[iteration],vdestinationfilefolder[iteration],vwildcard[iteration]));
				}
			}
		}
		else if (vsourcefilefolder!=null && vdestinationfilefolder!=null)
		{
			for (int i=0;i<vsourcefilefolder.length  && !parentJob.isStopped();i++)
			{
				if(!Const.isEmpty(vsourcefilefolder[i]) && !Const.isEmpty(vdestinationfilefolder[i]))
				{

					// ok we can process this file/folder
					
					if(log.isBasic()) logBasic(BaseMessages.getString(PKG, "JobCopyFiles.Log.ProcessingRow",vsourcefilefolder[i],vdestinationfilefolder[i],vwildcard[i]));
					
					if(!ProcessFileFolder(vsourcefilefolder[i],vdestinationfilefolder[i],vwildcard[i],parentJob,result))
					{
						// The copy process fail
						NbrFail++;
					}
				}
				else
				{
					if(log.isDetailed())			
						logDetailed(BaseMessages.getString(PKG, "JobCopyFiles.Log.IgnoringRow",vsourcefilefolder[i],vdestinationfilefolder[i],vwildcard[i]));
				}
			}
		}		
		
		// Check if all files was process with success
		if (NbrFail==0)
		{
			result.setResult( true );
			result.setNrErrors(0);	
		}else
		{
			result.setNrErrors(NbrFail);
		}

		
		return result;
	}

	private boolean ProcessFileFolder(String sourcefilefoldername,String destinationfilefoldername,String wildcard,Job parentJob,Result result)
	{
		boolean entrystatus = false ;
		FileObject sourcefilefolder = null;
		FileObject destinationfilefolder = null;
		
		// Clear list files to remove after copy process
		// This list is also added to result files name
		list_files_remove.clear();
		list_add_result.clear();
		
		
		// Get real source, destination file and wildcard
		String realSourceFilefoldername = environmentSubstitute(sourcefilefoldername);
		String realDestinationFilefoldername = environmentSubstitute(destinationfilefoldername);
		String realWildcard=environmentSubstitute(wildcard);

		try
		{
			sourcefilefolder = KettleVFS.getFileObject(realSourceFilefoldername, this);
			destinationfilefolder = KettleVFS.getFileObject(realDestinationFilefoldername, this);
			
			if (sourcefilefolder.exists())
			{
			
				// Check if destination folder/parent folder exists !
				// If user wanted and if destination folder does not exist
				// PDI will create it
				if(CreateDestinationFolder(destinationfilefolder))
				{

					// Basic Tests
					if (sourcefilefolder.getType().equals(FileType.FOLDER) && destination_is_a_file)//destinationfilefolder.getType().equals(FileType.FILE))
					{
						// Source is a folder, destination is a file
						// WARNING !!! CAN NOT COPY FOLDER TO FILE !!!
						
						log.logError(BaseMessages.getString(PKG, "JobCopyFiles.Log.Forbidden"), BaseMessages.getString(PKG, "JobCopyFiles.Log.CanNotCopyFolderToFile",realSourceFilefoldername,realDestinationFilefoldername));	
						
						NbrFail++;
						
					}
					else
					{
						
						if (destinationfilefolder.getType().equals(FileType.FOLDER) && sourcefilefolder.getType().equals(FileType.FILE) )
						{				
							// Source is a file, destination is a folder
							// Copy the file to the destination folder				
							
							destinationfilefolder.copyFrom(sourcefilefolder.getParent(),new TextOneFileSelector(sourcefilefolder.getParent().toString(),sourcefilefolder.getName().getBaseName(),destinationfilefolder.toString() ) );
							if(log.isDetailed())	
								log.logDetailed(BaseMessages.getString(PKG, "JobCopyFiles.Log.FileCopiedInfos"), 
									BaseMessages.getString(PKG, "JobCopyFiles.Log.FileCopied",sourcefilefolder.getName().toString(),destinationfilefolder.getName().toString()));
							
						}
						else if (sourcefilefolder.getType().equals(FileType.FILE) && destination_is_a_file)
						{
							// Source is a file, destination is a file

							destinationfilefolder.copyFrom(sourcefilefolder, new TextOneToOneFileSelector(destinationfilefolder));
						}
						else
						{
							// Both source and destination are folders
							if(log.isDetailed()) 
							{
								log.logDetailed("","  ");
								logDetailed(BaseMessages.getString(PKG, "JobCopyFiles.Log.FetchFolder",sourcefilefolder.toString()));
								
							}
							destinationfilefolder.copyFrom(sourcefilefolder,new TextFileSelector(sourcefilefolder.toString(),destinationfilefolder.toString(),realWildcard,parentJob) );
						}
						
						// Remove Files if needed
						if (remove_source_files && !list_files_remove.isEmpty())
						{
							 for (Iterator<String> iter = list_files_remove.iterator(); iter.hasNext() && !parentJob.isStopped();)
					        {
					            String fileremoventry = (String) iter.next();
					            // Remove ONLY Files
					            if (KettleVFS.getFileObject(fileremoventry, this).getType() == FileType.FILE)
					            {
						            boolean deletefile=KettleVFS.getFileObject(fileremoventry, this).delete();
						            log.logBasic(" ------ ");
						            if (!deletefile)
									{
										log.logError("      " + BaseMessages.getString(PKG, "JobCopyFiles.Log.Error"), 
												BaseMessages.getString(PKG, "JobCopyFiles.Error.Exception.CanRemoveFileFolder",fileremoventry));
									}
						            else
						            {
						            	if(log.isDetailed())
						            		log.logDetailed("      " + BaseMessages.getString(PKG, "JobCopyFiles.Log.FileFolderRemovedInfos"), 
						            			BaseMessages.getString(PKG, "JobCopyFiles.Log.FileFolderRemoved", fileremoventry));
						            }
					            }
					        }	
						}
						
						
						// Add files to result files name
						if (add_result_filesname && !list_add_result.isEmpty())
						{
							 for (Iterator<String> iter = list_add_result.iterator(); iter.hasNext();)
					        {
					            String fileaddentry = (String) iter.next();
					            // Add ONLY Files
					            if (KettleVFS.getFileObject(fileaddentry, this).getType() == FileType.FILE)
					            { 
				                	ResultFile resultFile = new ResultFile(ResultFile.FILE_TYPE_GENERAL, KettleVFS.getFileObject(fileaddentry, this), parentJob.getJobname(), toString());
				                    result.getResultFiles().put(resultFile.getFile().toString(), resultFile);
				                    if(log.isDetailed())
				                    {
				                    	log.logDetailed(""," ------ ");
				                    	log.logDetailed("      " + BaseMessages.getString(PKG, "JobCopyFiles.Log.ResultFilesName"),
						            		BaseMessages.getString(PKG, "JobCopyFiles.Log.FileAddedToResultFilesName",fileaddentry));
				                    }
					            }
					        }	
						}
					}
					entrystatus = true ;
				}	
				else
				{
					// Destination Folder or Parent folder is missing
					logError(BaseMessages.getString(PKG, "JobCopyFiles.Error.DestinationFolderNotFound",realDestinationFilefoldername));						
				}
			}
			else
			{
				logError(BaseMessages.getString(PKG, "JobCopyFiles.Error.SourceFileNotExists",realSourceFilefoldername));					
				
			}
		}
		catch (Exception e) 
		{
			log.logError("Error", BaseMessages.getString(PKG, "JobCopyFiles.Error.Exception.CopyProcess",realSourceFilefoldername.toString(),destinationfilefolder.toString(), e.getMessage()), e);					
		}
		finally 
		{
			if ( sourcefilefolder != null )
			{
				try  
				{
					sourcefilefolder.close();
					sourcefilefolder=null;
				}
				catch ( IOException ex ) {};
			}
			if ( destinationfilefolder != null )
			{
				try  
				{
					destinationfilefolder.close();
					destinationfilefolder=null;
				}
				catch ( IOException ex ) {};
			}
		}

		return entrystatus;
	}
	
	
	private class TextOneToOneFileSelector implements FileSelector 
	{
		FileObject destfile=null;
		
		public TextOneToOneFileSelector(FileObject destinationfile) 
		 {

			 if (destinationfile!=null)
			 {
				 destfile=destinationfile;
			 }
		 }
		 
		public boolean includeFile(FileSelectInfo info) 
		{
			boolean resultat=false;
			String fil_name=null;
			
			try
			{
					// check if the destination file exists
					
					if (destfile.exists())
					{
						if(log.isDetailed())
							log.logDetailed("      " + BaseMessages.getString(PKG, "JobCopyFiles.Log.FileExistsInfos"), BaseMessages.getString(PKG, "JobCopyFiles.Log.FileExists",destfile.toString()));//info.getFile().toString()));
						 
						if (overwrite_files) 
						{
							if(log.isDetailed())
								log.logDetailed("      " + BaseMessages.getString(PKG, "JobCopyFiles.Log.FileOverwriteInfos"),BaseMessages.getString(PKG, "JobCopyFiles.Log.FileOverwrite",destfile.toString()));
						
							resultat=true;
						}	
					}
					else
					{
						if(log.isDetailed())
							log.logDetailed("      " + BaseMessages.getString(PKG, "JobCopyFiles.Log.FileCopiedInfos"),BaseMessages.getString(PKG, "JobCopyFiles.Log.FileCopied",info.getFile().toString(),destfile.toString()));
						
						
						resultat= true;
					}
						
					
					
					if (resultat && remove_source_files)
					{
						// add this folder/file to remove files
						// This list will be fetched and all entries files
						// will be removed
						list_files_remove.add(info.getFile().toString());
					}
					
					if (resultat && add_result_filesname)
					{
						// add this folder/file to result files name
						list_add_result.add(destfile.toString());
					}
						
					
			}
			catch (Exception e) 
			{
				
				logError(BaseMessages.getString(PKG, "JobCopyFiles.Error.Exception.CopyProcess", info.getFile().toString(),fil_name, e.getMessage()));
					
				
			}
			
					
			return resultat;
			
		}
		public boolean traverseDescendents(FileSelectInfo info) 
		{
			return false;
		}
	}
	
	
	
	
	private boolean CreateDestinationFolder(FileObject filefolder)
	{
		FileObject folder=null;
		try
		{
			if(destination_is_a_file)
				folder=filefolder.getParent();
			else
				folder=filefolder;
			
    		if(!folder.exists())	
    		{
    			if(create_destination_folder)
    			{
        			if(log.isDetailed()) log.logDetailed("Folder  " + folder.getName() + " does not exist !");
        			folder.createFolder();
        			if(log.isDetailed()) log.logDetailed("Folder parent was created.");
    			}else
    			{
    				log.logError("Folder  " + folder.getName() + " does not exist !");
    				return false;
    			}
    		}
    		return true;
		}
		catch (Exception e) {
			logError("Couldn't created parent folder "+ folder.getName());
			
		}
		 finally {
         	if ( folder != null )
         	{
         		try  {
         			folder.close();
         			folder=null;
         		}
         		catch (Exception ex ) {};
         	}
         }
		 return false;
	}
	
	private class TextFileSelector implements FileSelector 
	{
		String file_wildcard=null,source_folder=null,destination_folder=null;
		Job parentjob;
		
		public TextFileSelector(String sourcefolderin,String destinationfolderin,String filewildcard, Job parentJob) 
		 {
			
			 if ( !Const.isEmpty(sourcefolderin))
			 {
				 source_folder=sourcefolderin;
			 }
			 if ( !Const.isEmpty(destinationfolderin))
			 {
				 destination_folder=destinationfolderin;
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
					
					String short_filename_from_basefolder=info.getFile().getName().toString().substring(source_folder.length(),info.getFile().getName().toString().length());
					String short_filename= info.getFile().getName().getBaseName();
					// Built destination filename
					file_name=KettleVFS.getFileObject(destination_folder + Const.FILE_SEPARATOR + short_filename_from_basefolder, JobEntryCopyFiles.this);//source_folder + Const.FILE_SEPARATOR + info.getFile().getName().getBaseName()); 
					
					if (!info.getFile().getParent().equals(info.getBaseFolder()))
					 {
						
						// Not in the Base Folder..Only if include sub folders  
						 if (include_subfolders)
						 {
							// Folders..only if include subfolders
							 if (info.getFile().getType() == FileType.FOLDER)
							 {
								 if (include_subfolders && copy_empty_folders && Const.isEmpty(file_wildcard))
								 {
									 if (!file_name.exists())
									 {
										if(log.isDetailed())
										{
											log.logDetailed(" ------ ");
											log.logDetailed("      " + BaseMessages.getString(PKG, "JobCopyFiles.Log.FolderCopiedInfos"),  BaseMessages.getString(PKG, "JobCopyFiles.Log.FolderCopied",info.getFile().toString(),file_name.toString()));
										}
										returncode= true;
									 }
									 else
									 {
										 if(log.isDetailed())
										 {
											log.logDetailed(" ------ ");
										 	log.logDetailed("      " + BaseMessages.getString(PKG, "JobCopyFiles.Log.FolderExistsInfos"), BaseMessages.getString(PKG, "JobCopyFiles.Log.FolderExists",file_name.toString()));
										 }
										 if (overwrite_files)
										 {
											 if(log.isDetailed())
												 log.logDetailed("      " + BaseMessages.getString(PKG, "JobCopyFiles.Log.FolderOverwriteInfos"),BaseMessages.getString(PKG, "JobCopyFiles.Log.FolderOverwrite",info.getFile().toString(),file_name.toString()));
											 returncode= true; 
										 }
									 } 
								 }
								 
							 }
							 else
							 {
								if (GetFileWildcard(short_filename,file_wildcard))
								{	
									// Check if the file exists
									 if (!file_name.exists())
									 {
										if(log.isDetailed())
										{
											log.logDetailed(" ------ ");
											log.logDetailed("      " + BaseMessages.getString(PKG, "JobCopyFiles.Log.FileCopiedInfos"),BaseMessages.getString(PKG, "JobCopyFiles.Log.FileCopied",info.getFile().toString(),file_name.toString()));
										}
										returncode= true;
									 }
									 else
									 {
										 if(log.isDetailed())
										 {
											 log.logDetailed(" ------ ");
											 log.logDetailed("      " + BaseMessages.getString(PKG, "JobCopyFiles.Log.FileExistsInfos"), BaseMessages.getString(PKG, "JobCopyFiles.Log.FileExists",file_name.toString()));
										 } 
										if (overwrite_files)
										 {
											if(log.isDetailed())
												log.logDetailed("       " + BaseMessages.getString(PKG, "JobCopyFiles.Log.FileExistsInfos"),BaseMessages.getString(PKG, "JobCopyFiles.Log.FileExists",info.getFile().toString(),file_name.toString()));
											 
											 returncode= true; 
										 }
									 }
								}
							 }
						 }
					 }
					 else
					 {
						// In the Base Folder...
						// Folders..only if include subfolders
						 if (info.getFile().getType() == FileType.FOLDER)
						 {
							 if (include_subfolders && copy_empty_folders  && Const.isEmpty(file_wildcard))
							 {
								 if (!file_name.exists())
								 {
									 if(log.isDetailed())
									 {
										 log.logDetailed(""," ------ ");							 
										 log.logDetailed("      " + BaseMessages.getString(PKG, "JobCopyFiles.Log.FolderCopiedInfos"),BaseMessages.getString(PKG, "JobCopyFiles.Log.FolderCopied",info.getFile().toString(),file_name.toString()));
									 }
									 
									 returncode= true; 
								 }
								 else
								 {
									 if(log.isDetailed())
									 {
										 log.logDetailed(""," ------ ");
										 log.logDetailed("      " + BaseMessages.getString(PKG, "JobCopyFiles.Log.FolderExistsInfos"), BaseMessages.getString(PKG, "JobCopyFiles.Log.FolderExists",file_name.toString()));
									 }
									 if (overwrite_files)
									 {
										 if(log.isDetailed())
											 log.logDetailed("      " + BaseMessages.getString(PKG, "JobCopyFiles.Log.FolderOverwriteInfos"),BaseMessages.getString(PKG, "JobCopyFiles.Log.FolderOverwrite",info.getFile().toString(),file_name.toString()));
											 
										 
										 returncode= true; 
									 }
								 }
								 
								 
							 
							 }
						 }
						 else
						 {
							 // file...Check if exists
							 if (GetFileWildcard(short_filename,file_wildcard))
							 {	
								 if (!file_name.exists())
								 {
									 if(log.isDetailed())
									 {
										 log.logDetailed(""," ------ ");
										 log.logDetailed("      " + BaseMessages.getString(PKG, "JobCopyFiles.Log.FileCopiedInfos"),BaseMessages.getString(PKG, "JobCopyFiles.Log.FileCopied",info.getFile().toString(),file_name.toString()));
									 }	
									 returncode= true;
									 
								 }
								 else
								 {
									 if(log.isDetailed())
									 {
										 log.logDetailed(""," ------ ");
										 log.logDetailed("      " + BaseMessages.getString(PKG, "JobCopyFiles.Log.FileExistsInfos"), BaseMessages.getString(PKG, "JobCopyFiles.Log.FileExists",file_name.toString()));
									 }
									 
									 if (overwrite_files)
									 {
										 if(log.isDetailed())
											 log.logDetailed("      " + BaseMessages.getString(PKG, "JobCopyFiles.Log.FileExistsInfos"),BaseMessages.getString(PKG, "JobCopyFiles.Log.FileExists",info.getFile().toString(),file_name.toString()));
									
										 returncode= true; 
									 } 
									 
								 }
							 }
						 }
						 
						 
						
					 }
					
				}
				
			}
			catch (Exception e) 
			{
				

				log.logError(BaseMessages.getString(PKG, "JobCopyFiles.Error.Exception.CopyProcessError") , BaseMessages.getString(PKG, "JobCopyFiles.Error.Exception.CopyProcess", 
					info.getFile().toString(), file_name.toString(), e.getMessage()));
				
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
			if (returncode && remove_source_files)
			{
				// add this folder/file to remove files
				// This list will be fetched and all entries files
				// will be removed
				list_files_remove.add(info.getFile().toString());
			}
			
			if (returncode && add_result_filesname)
			{
				// add this folder/file to result files name
				list_add_result.add(file_name.toString());
			}
			
			
			return returncode;
		}

		public boolean traverseDescendents(FileSelectInfo info) 
		{
			return true;
		}
	}
	private class TextOneFileSelector implements FileSelector 
	{
		String filename=null,foldername=null,destfolder=null;
		
		public TextOneFileSelector(String sourcefolderin, String sourcefilenamein,String destfolderin) 
		 {
			 if ( !Const.isEmpty(sourcefilenamein))
			 {
				 filename=sourcefilenamein;
			 }
			 
			 if ( !Const.isEmpty(sourcefolderin))
			 {
				 foldername=sourcefolderin;
			 }
			 if ( !Const.isEmpty(destfolderin))
			 {
				 destfolder=destfolderin;
			 }
		 }
		 
		public boolean includeFile(FileSelectInfo info) 
		{
			boolean resultat=false;
			String fil_name=null;
			
			try
			{

				if (info.getFile().getType() == FileType.FILE) 
				{
					if (info.getFile().getName().getBaseName().equals(filename) && (info.getFile().getParent().toString().equals(foldername))) 
					{
						// check if the file exists
						fil_name=destfolder + Const.FILE_SEPARATOR + filename;
						
						if (KettleVFS.getFileObject(fil_name, JobEntryCopyFiles.this).exists())
						{
							if(log.isDetailed())
								log.logDetailed("      " + BaseMessages.getString(PKG, "JobCopyFiles.Log.FileExistsInfos"), BaseMessages.getString(PKG, "JobCopyFiles.Log.FileExists",fil_name));
							 
							if (overwrite_files) 
							{
								if(log.isDetailed())
									log.logDetailed("      " + BaseMessages.getString(PKG, "JobCopyFiles.Log.FileOverwriteInfos"),BaseMessages.getString(PKG, "JobCopyFiles.Log.FileOverwrite",info.getFile().toString(),fil_name ));
							
								resultat=true;
							}
							
						}
						else
						{
		
							if(log.isDetailed()) log.logDetailed("      " + BaseMessages.getString(PKG, "JobCopyFiles.Log.FileCopiedInfos"),BaseMessages.getString(PKG, "JobCopyFiles.Log.FileCopied",info.getFile().toString(),fil_name));
							
							
							resultat=true;
						}
							
					}
					
					if (resultat && remove_source_files)
					{
						// add this folder/file to remove files
						// This list will be fetched and all entries files
						// will be removed
						list_files_remove.add(info.getFile().toString());
					}
					
					if (resultat && add_result_filesname)
					{
						// add this folder/file to result files name
						list_add_result.add(KettleVFS.getFileObject(fil_name, JobEntryCopyFiles.this).toString());
					}
				}		
					
			}
			catch (Exception e) 
			{
				
				log.logError(BaseMessages.getString(PKG, "JobCopyFiles.Error.Exception.CopyProcessError") , BaseMessages.getString(PKG, "JobCopyFiles.Error.Exception.CopyProcess", 
						info.getFile().toString(),fil_name, e.getMessage()));
					
				
				resultat= false;
			}
			
					
			return resultat;
			
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
	

	public void setCopyEmptyFolders(boolean copy_empty_foldersin) 
	{
		this.copy_empty_folders = copy_empty_foldersin;
	}
	
	public void setoverwrite_files(boolean overwrite_filesin) 
	{
		this.overwrite_files = overwrite_filesin;
	}

	public void setIncludeSubfolders(boolean include_subfoldersin) 
	{
		this.include_subfolders = include_subfoldersin;
	}
	
	public void setAddresultfilesname(boolean add_result_filesnamein) 
	{
		this.add_result_filesname = add_result_filesnamein;
	}
	
	
	public void setArgFromPrevious(boolean argfrompreviousin) 
	{
		this.arg_from_previous = argfrompreviousin;
	}
	
	public void setRemoveSourceFiles(boolean remove_source_filesin) 
	{
		this.remove_source_files = remove_source_filesin;
	}
	
	public void setDestinationIsAFile(boolean destination_is_a_file)
	{
		this.destination_is_a_file=destination_is_a_file;
	}
	
	public void setCreateDestinationFolder(boolean create_destination_folder)
	{
		this.create_destination_folder=create_destination_folder;
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

	    for (int i = 0; i < source_filefolder.length; i++) 
	    {
	      andValidator().validate(this, "arguments[" + i + "]", remarks, ctx);
	    } 
	  }

   public boolean evaluates() {
		return true;
   }
}