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
 
package org.pentaho.di.job.entries.zipfile;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.Deflater;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.apache.commons.vfs.FileObject;
import org.eclipse.swt.widgets.Shell;
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
import org.pentaho.di.job.JobMeta;
import org.pentaho.di.job.entry.JobEntryBase;
import org.pentaho.di.job.entry.JobEntryDialogInterface;
import org.pentaho.di.job.entry.JobEntryInterface;
import org.pentaho.di.repository.Repository;
import org.w3c.dom.Node;


/**
 * This defines a 'zip file' job entry. Its main use would be to 
 * zip files in a directory and process zipped files (deleted or move)
 * 
 * @author Samatar Hassan
 * @since 27-02-2007
 *
 */
public class JobEntryZipFile extends JobEntryBase implements Cloneable, JobEntryInterface
{
	private String zipFilename;
	public int compressionrate;
	public int ifzipfileexists;
	public int afterzip;
	private String wildcard;
	private String wildcardexclude;
	private String sourcedirectory;
	private String movetodirectory;
	

	public JobEntryZipFile(String n)
	{
		super(n, "");
		zipFilename=null;
		ifzipfileexists=2;
		afterzip=0;
		compressionrate=1;
		wildcard=null;
		wildcardexclude=null;
		sourcedirectory=null;
		movetodirectory=null;
		setID(-1L);
		setType(JobEntryInterface.TYPE_JOBENTRY_ZIP_FILE);
	}

	public JobEntryZipFile()
	{
		this("");
	}

	public JobEntryZipFile(JobEntryBase jeb)
	{
		super(jeb);
	}

    public Object clone()
    {
        JobEntryZipFile je = (JobEntryZipFile) super.clone();
        return je;
    }
    
	public String getXML()
	{
        StringBuffer retval = new StringBuffer(50);
		
		retval.append(super.getXML());		
		retval.append("      ").append(XMLHandler.addTagValue("zipfilename",      zipFilename));
		retval.append("      ").append(XMLHandler.addTagValue("compressionrate",  compressionrate));
		retval.append("      ").append(XMLHandler.addTagValue("ifzipfileexists",  ifzipfileexists));
		retval.append("      ").append(XMLHandler.addTagValue("wildcard",         wildcard));
		retval.append("      ").append(XMLHandler.addTagValue("wildcardexclude",  wildcardexclude));
		retval.append("      ").append(XMLHandler.addTagValue("sourcedirectory",  sourcedirectory));
		retval.append("      ").append(XMLHandler.addTagValue("movetodirectory",  movetodirectory));
		retval.append("      ").append(XMLHandler.addTagValue("afterzip",  afterzip));
		return retval.toString();
	}
	
	public void loadXML(Node entrynode, List<DatabaseMeta> databases, Repository rep)
		throws KettleXMLException
	{
		try
		{
			super.loadXML(entrynode, databases);
			zipFilename = XMLHandler.getTagValue(entrynode, "zipfilename");
			compressionrate = Const.toInt(XMLHandler.getTagValue(entrynode, "compressionrate"), -1);
			ifzipfileexists = Const.toInt(XMLHandler.getTagValue(entrynode, "ifzipfileexists"), -1);
			afterzip        = Const.toInt(XMLHandler.getTagValue(entrynode, "afterzip"), -1);

    		wildcard = XMLHandler.getTagValue(entrynode, "wildcard");
			wildcardexclude = XMLHandler.getTagValue(entrynode, "wildcardexclude");
			sourcedirectory = XMLHandler.getTagValue(entrynode, "sourcedirectory");
			movetodirectory = XMLHandler.getTagValue(entrynode, "movetodirectory");

		}
		catch(KettleXMLException xe)
		{
			throw new KettleXMLException("Unable to load job entry of type 'zipfile' from XML node", xe);
		}
	}

	public void loadRep(Repository rep, long id_jobentry, List<DatabaseMeta> databases)
		throws KettleException
	{
		
		try
		{
			super.loadRep(rep, id_jobentry, databases);
			zipFilename = rep.getJobEntryAttributeString(id_jobentry, "zipfilename");
			compressionrate=(int) rep.getJobEntryAttributeInteger(id_jobentry, "compressionrate");
			ifzipfileexists=(int) rep.getJobEntryAttributeInteger(id_jobentry, "ifzipfileexists");
			afterzip=(int) rep.getJobEntryAttributeInteger(id_jobentry, "afterzip");
			wildcard = rep.getJobEntryAttributeString(id_jobentry, "wildcard");
			wildcardexclude = rep.getJobEntryAttributeString(id_jobentry, "wildcardexclude");
			sourcedirectory = rep.getJobEntryAttributeString(id_jobentry, "sourcedirectory");
			movetodirectory = rep.getJobEntryAttributeString(id_jobentry, "movetodirectory");
		
		}


		catch(KettleException dbe)
		{
			throw new KettleException("Unable to load job entry of type 'zipfile' from the repository for id_jobentry="+id_jobentry, dbe);
		}
	}
	
	public void saveRep(Repository rep, long id_job)
		throws KettleException
	{
		try
		{
			super.saveRep(rep, id_job);
			
			rep.saveJobEntryAttribute(id_job, getID(), "zipfilename", zipFilename);
			rep.saveJobEntryAttribute(id_job, getID(), "compressionrate", compressionrate);
			rep.saveJobEntryAttribute(id_job, getID(), "ifzipfileexists", ifzipfileexists);
			rep.saveJobEntryAttribute(id_job, getID(), "afterzip", afterzip);

			rep.saveJobEntryAttribute(id_job, getID(), "wildcard", wildcard);
			rep.saveJobEntryAttribute(id_job, getID(), "wildcardexclude", wildcardexclude);
			rep.saveJobEntryAttribute(id_job, getID(), "sourcedirectory", sourcedirectory);
			rep.saveJobEntryAttribute(id_job, getID(), "movetodirectory", movetodirectory);
		}
		catch(KettleDatabaseException dbe)
		{
			throw new KettleException("Unable to save job entry of type 'zipfile' to the repository for id_job="+id_job, dbe);
		}
	}

	
	public Result execute(Result previousResult, int nr, Repository rep, Job parentJob)
	{
		LogWriter log = LogWriter.getInstance();
		Result result = previousResult;
		result.setResult( false );
		boolean Fileexists=false;

		String realZipfilename       = environmentSubstitute(zipFilename);
		String realWildcard          = environmentSubstitute(wildcard);
		String realWildcardExclude   = environmentSubstitute(wildcardexclude);
		String realTargetdirectory   = environmentSubstitute(sourcedirectory);
		String realMovetodirectory   = environmentSubstitute(movetodirectory);
	
		if (realZipfilename!=null)
		{
            FileObject fileObject = null;
			try {
				fileObject = KettleVFS.getFileObject(realZipfilename);
				// Check if Zip File exists
				if ( fileObject.exists() )
				{
					Fileexists =true;
					log.logDebug(toString(), Messages.getString("JobZipFiles.Zip_FileExists1.Label")+ realZipfilename 
											+ Messages.getString("JobZipFiles.Zip_FileExists2.Label"));
				}
				else
				{
					Fileexists =false;
				}

	
				// Let's start the process now
				if (ifzipfileexists==3 && Fileexists)
				{
					// the zip file exists and user want to Fail
					result.setResult( false );
					result.setNrErrors(1);

				}
				else if(ifzipfileexists==2 && Fileexists)
				{
					// the zip file exists and user want to do nothing
					result.setResult( true );

				}
				else if(afterzip==2 && realMovetodirectory== null)
				{
					// After Zip, Move files..User must give a destination Folder
					result.setResult( false );
					result.setNrErrors(1);
					log.logError(toString(), Messages.getString("JobZipFiles.AfterZip_No_DestinationFolder_Defined.Label"));

				}				
				else 
					// After Zip, Move files..User must give a destination Folder
				{

					if(ifzipfileexists==0 && Fileexists)
					{

						// the zip file exists and user want to create new one with unique name
						//Format Date
		
						DateFormat dateFormat = new SimpleDateFormat("hhmmss_mmddyyyy");
						realZipfilename=realZipfilename + "_" + dateFormat.format(new Date())+".zip";		
						log.logDebug(toString(), Messages.getString("JobZipFiles.Zip_FileNameChange1.Label") + realZipfilename + 
												Messages.getString("JobZipFiles.Zip_FileNameChange1.Label"));


					}
					else if(ifzipfileexists==1 && Fileexists)
					{
						log.logDebug(toString(), Messages.getString("JobZipFiles.Zip_FileAppend1.Label") + realZipfilename + 
										Messages.getString("JobZipFiles.Zip_FileAppend2.Label"));
					}
				

					// Get all the files in the directory...

					File f = new File(realTargetdirectory);

					String [] filelist = f.list();

					log.logDetailed(toString(), Messages.getString("JobZipFiles.Files_Found1.Label") +filelist.length+ 
										Messages.getString("JobZipFiles.Files_Found2.Label") + realTargetdirectory + 
										Messages.getString("JobZipFiles.Files_Found3.Label"));

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

					// Prepare Zip File
					byte[] buffer = new byte[18024];
					
					FileOutputStream dest = new FileOutputStream(realZipfilename);
					BufferedOutputStream buff = new BufferedOutputStream(dest);
					ZipOutputStream out = new ZipOutputStream(buff);


					// Set the method
					out.setMethod(ZipOutputStream.DEFLATED);

					// Set the compression level
					if (compressionrate==0)
					{
						out.setLevel(Deflater.NO_COMPRESSION);
					}
					else if (compressionrate==1)
					{
						out.setLevel(Deflater.DEFAULT_COMPRESSION);
					}
					if (compressionrate==2)
					{
						out.setLevel(Deflater.BEST_COMPRESSION);
					}
					if (compressionrate==3)
					{
						out.setLevel(Deflater.BEST_SPEED);
					}

					// Specify Zipped files (After that we will move,delete them...)
					String[] ZippedFiles = new String[filelist.length];
					int FileNum=0;			

					// Get the files in the list...
					for (int i=0;i<filelist.length && !parentJob.isStopped();i++)
					{
						boolean getIt = true;
						boolean getItexclude = false;
			
				
						// First see if the file matches the regular expression!
						if (pattern!=null)
						{
							Matcher matcher = pattern.matcher(filelist[i]);
							getIt = matcher.matches();
						}

						if (patternexclude!=null)
						{
							Matcher matcherexclude = patternexclude.matcher(filelist[i]);
							getItexclude = matcherexclude.matches();
						}
						
						// Get processing File
						String targetFilename = realTargetdirectory+Const.FILE_SEPARATOR+filelist[i];
						File file = new File(targetFilename);

						if (getIt && !getItexclude && !file.isDirectory())
						{

							// We can add the file to the Zip Archive

							log.logDebug(toString(), Messages.getString("JobZipFiles.Add_FilesToZip1.Label")+filelist[i]+
										Messages.getString("JobZipFiles.Add_FilesToZip2.Label")+realTargetdirectory+
										Messages.getString("JobZipFiles.Add_FilesToZip3.Label"));
							
							// Associate a file input stream for the current file
							FileInputStream in = new FileInputStream(targetFilename);

							// Add ZIP entry to output stream.
							out.putNextEntry(new ZipEntry(filelist[i]));

	
							int len;
							while ((len = in.read(buffer)) > 0)
							{
								out.write(buffer, 0, len);
							}

							out.closeEntry();

							// Close the current file input stream
							in.close(); 

							// Get Zipped File
							ZippedFiles[FileNum] = filelist[i];
							FileNum=FileNum+1;
						}
					}
						
					// Close the ZipOutPutStream
					out.close();

					//-----Get the list of Zipped Files and Move or Delete Them
					if (afterzip == 1 || afterzip == 2)
					{
						// iterate through the array of Zipped files
						for (int i = 0; i < ZippedFiles.length; i++) 
						{
							if ( ZippedFiles[i] != null)
							{
								// Delete File
								FileObject fileObjectd = KettleVFS.getFileObject(realTargetdirectory+Const.FILE_SEPARATOR+ZippedFiles[i]);

								// Here gc() is explicitly called if e.g. createfile is used in the same
								// job for the same file. The problem is that after creating the file the
								// file object is not properly garbaged collected and thus the file cannot
								// be deleted anymore. This is a known problem in the JVM.
								
								System.gc();

								// Here we can move, delete files
								if (afterzip == 1)
								{
									// Delete File
									boolean deleted = fileObjectd.delete();
									if ( ! deleted )
									{	
						    			result.setResult( false );
										result.setNrErrors(1);
										log.logError(toString(), Messages.getString("JobZipFiles.Cant_Delete_File1.Label")+
											realTargetdirectory+Const.FILE_SEPARATOR+ZippedFiles[i]+
												Messages.getString("JobZipFiles.Cant_Delete_File2.Label"));

									}
									// File deleted
									log.logDebug(toString(), Messages.getString("JobZipFiles.File_Deleted1.Label") + 
										realTargetdirectory+Const.FILE_SEPARATOR+ZippedFiles[i] + 
										Messages.getString("JobZipFiles.File_Deleted2.Label"));
								}
								else if(afterzip == 2)
								{
									// Move File	
									try
									{
										FileObject fileObjectm = KettleVFS.getFileObject(realMovetodirectory + Const.FILE_SEPARATOR+ZippedFiles[i]);
										fileObjectd.moveTo(fileObjectm);
									}
									catch (IOException e) 
									{
										log.logError(toString(), Messages.getString("JobZipFiles.Cant_Move_File1.Label") +ZippedFiles[i]+
											Messages.getString("JobZipFiles.Cant_Move_File2.Label") + e.getMessage());
										result.setResult( false );
										result.setNrErrors(1);				
									}
									// File moved
									log.logDebug(toString(), Messages.getString("JobZipFiles.File_Moved1.Label") + ZippedFiles[i] + 
										Messages.getString("JobZipFiles.File_Moved2.Label"));
								 }
							}
						}
					}
					result.setResult( true );
				}
			}
			catch (IOException e) 
			{
       			log.logError(toString(), Messages.getString("JobZipFiles.Cant_CreateZipFile1.Label") +realZipfilename+
		       							 Messages.getString("JobZipFiles.Cant_CreateZipFile2.Label") + e.getMessage());
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
			}
		}
		else
		{	
			result.setResult( false );
			result.setNrErrors(1);
			log.logError(toString(), Messages.getString("JobZipFiles.No_ZipFile_Defined.Label"));
		}
		
		return result;
	}

	public boolean evaluates()
	{
		return true;
	}
    
    public JobEntryDialogInterface getDialog(Shell shell,JobEntryInterface jei,JobMeta jobMeta,String jobName,Repository rep) {
        return new JobEntryZipFileDialog(shell,this,jobMeta);
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
	
	public void setSourceDirectory(String sourcedirectory)
	{
		this.sourcedirectory = sourcedirectory;
	}
	
	public void setMoveToDirectory(String movetodirectory)
	{
		this.movetodirectory = movetodirectory;
	}
	
	public String getSourceDirectory()
	{
		return sourcedirectory;
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
}