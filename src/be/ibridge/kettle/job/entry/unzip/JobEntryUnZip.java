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
 
package be.ibridge.kettle.job.entry.unzip;

import java.io.*;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.*;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.io.FileOutputStream;

import org.eclipse.swt.widgets.Shell;
import org.w3c.dom.Node;
import be.ibridge.kettle.core.Const;
import be.ibridge.kettle.core.LogWriter;
import be.ibridge.kettle.core.Result;
import be.ibridge.kettle.core.ResultFile;
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


/**
 * This defines a 'unzip' job entry. Its main use would be to 
 * unzip files in a directory
 * 
 * @author Samatar Hassan
 * @since 25-09-2007
 *
 */
public class JobEntryUnZip extends JobEntryBase implements Cloneable, JobEntryInterface
{
	private String zipFilename;
	public int afterunzip;
	private String wildcard;
	private String wildcardexclude;
	private String targetdirectory;
	private String movetodirectory;
	private boolean addfiletoresult;
	

	public JobEntryUnZip(String n)
	{
		super(n, "");
		zipFilename=null;
		afterunzip=0;
		wildcard=null;
		wildcardexclude=null;
		targetdirectory=null;
		movetodirectory=null;
		addfiletoresult = false;
		setID(-1L);
		setType(JobEntryInterface.TYPE_JOBENTRY_UNZIP);
	}

	public JobEntryUnZip()
	{
		this("");
	}

	public JobEntryUnZip(JobEntryBase jeb)
	{
		super(jeb);
	}

    public Object clone()
    {
        JobEntryUnZip je = (JobEntryUnZip) super.clone();
        return je;
    }
    
	public String getXML()
	{
        StringBuffer retval = new StringBuffer(50);
		
		retval.append(super.getXML());		
		retval.append("      ").append(XMLHandler.addTagValue("zipfilename",      zipFilename));
		retval.append("      ").append(XMLHandler.addTagValue("wildcard",         wildcard));
		retval.append("      ").append(XMLHandler.addTagValue("wildcardexclude",  wildcardexclude));
		retval.append("      ").append(XMLHandler.addTagValue("targetdirectory",  targetdirectory));
		retval.append("      ").append(XMLHandler.addTagValue("movetodirectory",  movetodirectory));
		retval.append("      ").append(XMLHandler.addTagValue("afterunzip",  afterunzip));
		retval.append("      ").append(XMLHandler.addTagValue("addfiletoresult",  addfiletoresult));
		return retval.toString();
	}
	
	public void loadXML(Node entrynode, ArrayList databases, Repository rep)
		throws KettleXMLException
	{
		try
		{
			super.loadXML(entrynode, databases);
			zipFilename = XMLHandler.getTagValue(entrynode, "zipfilename");
			afterunzip        = Const.toInt(XMLHandler.getTagValue(entrynode, "afterunzip"), -1);

    		wildcard = XMLHandler.getTagValue(entrynode, "wildcard");
			wildcardexclude = XMLHandler.getTagValue(entrynode, "wildcardexclude");
			targetdirectory = XMLHandler.getTagValue(entrynode, "targetdirectory");
			movetodirectory = XMLHandler.getTagValue(entrynode, "movetodirectory");
			addfiletoresult = "Y".equalsIgnoreCase(XMLHandler.getTagValue(entrynode, "addfiletoresult"));	

		}
		catch(KettleXMLException xe)
		{
			throw new KettleXMLException("Unable to load job entry of type 'unzip' from XML node", xe);
		}
	}

	public void loadRep(Repository rep, long id_jobentry, ArrayList databases)
		throws KettleException
	{
		
		try
		{
			super.loadRep(rep, id_jobentry, databases);
			zipFilename = rep.getJobEntryAttributeString(id_jobentry, "zipfilename");
			afterunzip=(int) rep.getJobEntryAttributeInteger(id_jobentry, "afterunzip");
			wildcard = rep.getJobEntryAttributeString(id_jobentry, "wildcard");
			wildcardexclude = rep.getJobEntryAttributeString(id_jobentry, "wildcardexclude");
			targetdirectory = rep.getJobEntryAttributeString(id_jobentry, "targetdirectory");
			movetodirectory = rep.getJobEntryAttributeString(id_jobentry, "movetodirectory");
			addfiletoresult=rep.getJobEntryAttributeBoolean(id_jobentry, "addfiletoresult");
		
		}


		catch(KettleException dbe)
		{
			throw new KettleException("Unable to load job entry of type 'unzip' from the repository for id_jobentry="+id_jobentry, dbe);
		}
	}
	
	public void saveRep(Repository rep, long id_job)
		throws KettleException
	{
		try
		{
			super.saveRep(rep, id_job);
			
			rep.saveJobEntryAttribute(id_job, getID(), "zipfilename", zipFilename);
			rep.saveJobEntryAttribute(id_job, getID(), "afterunzip", afterunzip);

			rep.saveJobEntryAttribute(id_job, getID(), "wildcard", wildcard);
			rep.saveJobEntryAttribute(id_job, getID(), "wildcardexclude", wildcardexclude);
			rep.saveJobEntryAttribute(id_job, getID(), "targetdirectory", targetdirectory);
			rep.saveJobEntryAttribute(id_job, getID(), "movetodirectory", movetodirectory);
			rep.saveJobEntryAttribute(id_job, getID(), "addfiletoresult", addfiletoresult);
		}
		catch(KettleDatabaseException dbe)
		{
			throw new KettleException("Unable to save job entry of type 'unzip' to the repository for id_job="+id_job, dbe);
		}
	}

	
	public Result execute(Result previousResult, int nr, Repository rep, Job parentJob)
	{
		LogWriter log = LogWriter.getInstance();
		Result result = previousResult;
		result.setResult( false );

		String realZipfilename       = StringUtil.environmentSubstitute(zipFilename);
		String realWildcard          = StringUtil.environmentSubstitute(wildcard);
		String realWildcardExclude   = StringUtil.environmentSubstitute(wildcardexclude);
		String realTargetdirectory   = StringUtil.environmentSubstitute(targetdirectory);
		String realMovetodirectory   = StringUtil.environmentSubstitute(movetodirectory);
		
		//File tempFile = null;
		//File fileZip =null;
		
		if(!Const.isEmpty(zipFilename))
		{
			FileObject fileObject = null;
			FileObject targetdir=null;
			FileObject movetodir=null;
			
			ZipFile zipfile=null;
			
			try 
			{
				// Let's check zip file
				fileObject = KettleVFS.getFileObject(realZipfilename);	
				if ( fileObject.exists())
				{
					if (log.isDetailed()) log.logDetailed(toString(),Messages.getString("JobUnZip.Zip_FileExists.Label",realZipfilename));
					// Let's check target folder
					if(!Const.isEmpty(targetdirectory))
					{
						targetdir = KettleVFS.getFileObject(realTargetdirectory);	
						if ((targetdir.exists()) && (targetdir.getType() == FileType.FOLDER))
						{
							if (log.isDetailed()) log.logDetailed(toString(),Messages.getString("JobUnZip.TargetFolderNotExists.Label",realTargetdirectory));
							
							// If user want to move zip files after process
							// movetodirectory must be provided 
							if((afterunzip==2) && (Const.isEmpty(movetodirectory)))
							{
								log.logError(Messages.getString("JobUnZip.Error.Label"), Messages.getString("JobUnZip.MoveToDirectoryEmpty.Label"));
							}
							else
							{
								boolean move_tofolder=false;
								if(afterunzip==2)
								{
									movetodir = KettleVFS.getFileObject(realMovetodirectory);
									
									if  (!movetodir.exists()) move_tofolder=true;
									
								}
								
								if(move_tofolder)
								{
									log.logError(Messages.getString("JobUnZip.Error.Label"), Messages.getString("JobUnZip.MoveToDirectoryNotExists.Label"));
								
								}
								else
								{
									// We can now start the unzip process ...
									zipfile = new ZipFile(new File(realZipfilename));
									Enumeration zipEnum = zipfile.entries();
									
									File folder = new File(realTargetdirectory);
									String foldername=folder.getAbsolutePath();
									
									Pattern pattern = null;
									if (!Const.isEmpty(realWildcard)) 
									{
										pattern = Pattern.compile(realWildcard);
								
									}
									Pattern patternexclude = null;
									if (!Const.isEmpty(realWildcardExclude)) 
									{
										patternexclude = Pattern.compile(realWildcardExclude);
								
									}
									
									  while( zipEnum.hasMoreElements() )
							          {
										  ZipEntry item = (ZipEntry) zipEnum.nextElement();
										  
										  if( item.isDirectory())
										  {
											 // Directory 
								             File newdir = new File( foldername+ Const.FILE_SEPARATOR + item.getName() );
								             if(log.isDetailed()) log.logDetailed(toString(),Messages.getString("JobUnZip.CreatingDirectory.Label",newdir.getAbsolutePath()));
								             // Create Directory if necessary ...
								             if(!newdir.exists())  newdir.mkdir();

										  }
										  else
										  {
											// File
											boolean getIt = true;
											boolean getItexclude = false;
												
										    // First see if the file matches the regular expression!
											if (pattern!=null)
											{
												Matcher matcher = pattern.matcher(item.getName());
												getIt = matcher.matches();
											}

											if (patternexclude!=null)
											{
												Matcher matcherexclude = patternexclude.matcher(item.getName());
												getItexclude = matcherexclude.matches();
											}  
											  
											if (getIt && !getItexclude)
											{
											  
												String newfile = foldername+ Const.FILE_SEPARATOR + item.getName();
								                    
												if(log.isDetailed()) log.logDetailed(toString(),Messages.getString("JobUnZip.ExtractingEntry.Label",item.getName(),newfile));
												
								                InputStream is = zipfile.getInputStream(item);
								                FileOutputStream fos = new FileOutputStream(newfile);
								                  
								                if(is!=null)
								                {
									                byte[] buff=new byte[2048];
									                int len;
								                	
								                	while((len=is.read(buff))>0)
								                	{
								                		fos.write(buff,0,len);
								                	}
	
								                	if(is!=null) is.close();
								                	if(fos!=null) fos.close();	
									                
									                if (addfiletoresult)
													{
									                	// Add file to result files name
									                	ResultFile resultFile = new ResultFile(ResultFile.FILE_TYPE_GENERAL , KettleVFS.getFileObject(newfile), parentJob.getName(), toString());
									                    result.getResultFiles().put(resultFile.getFile().toString(), resultFile);
													}
								                }
											}    		
										  }
  
							          }

									  zipfile.close();  
									  
									 // Here gc() is explicitly called if e.g. createfile is used in the same
									 // job for the same file. The problem is that after creating the file the
									 // file object is not properly garbaged collected and thus the file cannot
									 // be deleted anymore. This is a known problem in the JVM.
										
									 System.gc();
									  
									  // Unzip done...
									  if (afterunzip==1)
									  {
										  // delete zip file
										  boolean deleted = fileObject.delete();
										  if ( ! deleted )	
										  {	
								    			result.setResult( false );
												result.setNrErrors(1);
												log.logError(toString(), Messages.getString("JobUnZip.Cant_Delete_File.Label",realZipfilename));

											}
											// File deleted
											log.logDebug(toString(), Messages.getString("JobUnZip.File_Deleted.Label",realZipfilename));
										  
									  }
									  else if(afterunzip == 2)
									  {
											// Move File	
											try
											{
												fileObject.moveTo(movetodir);
											}
											catch (IOException e) 
											{
												log.logError(toString(), Messages.getString("JobUnZip.Cant_Move_File.Label",realZipfilename,realMovetodirectory,e.getMessage()));
												result.setResult( false );
												result.setNrErrors(1);				
											}
											// File moved
											log.logDebug(toString(), Messages.getString("JobUnZip.File_Moved.Label",realZipfilename,realMovetodirectory));
									 }
									  
									  
									  result.setResult( true );

									
								}
							}
						
						}
						else
						{
							log.logError(Messages.getString("JobUnZip.Error.Label"), Messages.getString("JobUnZip.TargetFolderNotFound.Label"));
						}
					}
					else
					{
						log.logError(Messages.getString("JobUnZip.Error.Label"), Messages.getString("JobUnZip.TargetFolderMissing.Label"));
					}
					
				
				}
				else
				{
					log.logError(Messages.getString("JobUnZip.Error.Label"), Messages.getString("JobUnZip.ZipFile.NotExists.Label",realZipfilename));
				}
				
			}
			catch (IOException e) 
			{
       			log.logError(Messages.getString("JobUnZip.Error.Label"), Messages.getString("JobUnZip.ErrorUnzip.Label",realZipfilename,e.getMessage()));
				result.setResult( false );
				result.setNrErrors(1);				
			}
			finally 
			{
				if ( fileObject != null )
				{
					try  
					{
						fileObject.close();
					}
					catch ( IOException ex ) {};
				}
				if ( targetdir != null )
				{
					try  
					{
						targetdir.close();
					}
					catch ( IOException ex ) {};
				}
				if ( movetodir != null )
				{
					try  
					{
						movetodir.close();
					}
					catch ( IOException ex ) {};
				}
				if ( zipfile != null )
				{
					try  
					{
						zipfile.close();
					}
					catch ( IOException ex ) {};
				}
				
				
				
			}
		
		}
		else
		{
			// Zipfile is missing
			log.logError(toString(), Messages.getString("JobUnZip.No_ZipFile_Defined.Label"));
		}
	
		
		return result;
	}

	public boolean evaluates()
	{
		return true;
	}
    
    public JobEntryDialogInterface getDialog(Shell shell,JobEntryInterface jei,JobMeta jobMeta,String jobName,Repository rep) {
        return new JobEntryUnZipDialog(shell,this,jobMeta);
    }

	public void setZipFilename(String zipFilename)
	{
		this.zipFilename = zipFilename;
	}
	
	public void setWildcard(String wildcard)
	{
		this.wildcard = wildcard;
	}
	public void setWildcardExclude(String wildcardexclude)
	{
		this.wildcardexclude = wildcardexclude;
	}
	
	public void setSourceDirectory(String targetdirectoryin)
	{
		this.targetdirectory = targetdirectoryin;
	}
	
	public void setMoveToDirectory(String movetodirectory)
	{
		this.movetodirectory = movetodirectory;
	}
	
	public String getSourceDirectory()
	{
		return targetdirectory;
	}

	public String getMoveToDirectory()
	{
		return movetodirectory;
	}

	public String getZipFilename()
	{
		return zipFilename;
	}

	public String getWildcard()
	{
		return wildcard;
	}
	
	public String getWildcardExclude()
	{
		return wildcardexclude;
	}
	public void setAddFileToResult(boolean addfiletoresultin) 
	{
		this.addfiletoresult = addfiletoresultin;
	}
	
	public boolean isAddFileToResult() 
	{
		return addfiletoresult;
	}

	
}