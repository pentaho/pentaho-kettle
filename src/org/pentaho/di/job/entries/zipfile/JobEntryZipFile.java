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

package org.pentaho.di.job.entries.zipfile;

import static org.pentaho.di.job.entry.validator.AbstractFileValidator.putVariableSpace;
import static org.pentaho.di.job.entry.validator.AndValidator.putValidators;
import static org.pentaho.di.job.entry.validator.FileDoesNotExistValidator.putFailIfExists;
import static org.pentaho.di.job.entry.validator.JobEntryValidatorUtils.andValidator;
import static org.pentaho.di.job.entry.validator.JobEntryValidatorUtils.fileDoesNotExistValidator;
import static org.pentaho.di.job.entry.validator.JobEntryValidatorUtils.notBlankValidator;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.Deflater;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import org.apache.commons.vfs.FileObject;
import org.apache.commons.vfs.FileSelectInfo;
import org.apache.commons.vfs.FileSelector;
import org.apache.commons.vfs.FileSystemException;
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
import org.pentaho.di.core.util.StringUtil;
import org.pentaho.di.core.vfs.KettleVFS;
import org.pentaho.di.core.xml.XMLHandler;
import org.pentaho.di.job.Job;
import org.pentaho.di.job.JobEntryType;
import org.pentaho.di.job.JobMeta;
import org.pentaho.di.job.entry.JobEntryBase;
import org.pentaho.di.job.entry.JobEntryInterface;
import org.pentaho.di.job.entry.validator.ValidatorContext;
import org.pentaho.di.repository.Repository;
import org.w3c.dom.Node;

/**
 * This defines a 'zip file' job entry. Its main use would be to
 * zip files in a directory and process zipped files (deleted or move).
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
	private boolean addfiletoresult;
	private boolean isfromprevious;
	private boolean createparentfolder;
	private boolean adddate;
	private boolean addtime;
	private boolean SpecifyFormat;
	private String date_time_format;
	private boolean createMoveToDirectory;
	private boolean includingSubFolders;

	/**
	 * Default constructor.
	 */
	public JobEntryZipFile(String n)
	{
		super(n, "");
		date_time_format=null;
		zipFilename=null;
		ifzipfileexists=2;
		afterzip=0;
		compressionrate=1;
		wildcard=null;
		wildcardexclude=null;
		sourcedirectory=null;
		movetodirectory=null;
		addfiletoresult = false;
		isfromprevious = false;
		createparentfolder = false;
		adddate=false;
		addtime=false;
		SpecifyFormat=false;
		createMoveToDirectory=false;
		setID(-1L);
		setJobEntryType(JobEntryType.ZIP_FILE);
		includingSubFolders=true;
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
        StringBuffer retval = new StringBuffer(500);
		
		retval.append(super.getXML());		
		retval.append("      ").append(XMLHandler.addTagValue("zipfilename",      zipFilename));
		retval.append("      ").append(XMLHandler.addTagValue("compressionrate",  compressionrate));
		retval.append("      ").append(XMLHandler.addTagValue("ifzipfileexists",  ifzipfileexists));
		retval.append("      ").append(XMLHandler.addTagValue("wildcard",         wildcard));
		retval.append("      ").append(XMLHandler.addTagValue("wildcardexclude",  wildcardexclude));
		retval.append("      ").append(XMLHandler.addTagValue("sourcedirectory",  sourcedirectory));
		retval.append("      ").append(XMLHandler.addTagValue("movetodirectory",  movetodirectory));
		retval.append("      ").append(XMLHandler.addTagValue("afterzip",  afterzip));
		retval.append("      ").append(XMLHandler.addTagValue("addfiletoresult",  addfiletoresult));
		retval.append("      ").append(XMLHandler.addTagValue("isfromprevious",  isfromprevious));
		retval.append("      ").append(XMLHandler.addTagValue("createparentfolder",  createparentfolder));
		retval.append("      ").append(XMLHandler.addTagValue("adddate",  adddate));
		retval.append("      ").append(XMLHandler.addTagValue("addtime",  addtime));
		retval.append("      ").append(XMLHandler.addTagValue("SpecifyFormat",  SpecifyFormat));
		retval.append("      ").append(XMLHandler.addTagValue("date_time_format",  date_time_format));
		retval.append("      ").append(XMLHandler.addTagValue("createMoveToDirectory",  createMoveToDirectory));
        retval.append("      ").append(XMLHandler.addTagValue("include_subfolders",  includingSubFolders));
		
		return retval.toString();
	}
	
	public void loadXML(Node entrynode, List<DatabaseMeta> databases, List<SlaveServer> slaveServers, Repository rep)
	throws KettleXMLException
    {
	    try
	    {
	    	super.loadXML(entrynode, databases, slaveServers);
			zipFilename = XMLHandler.getTagValue(entrynode, "zipfilename");
			compressionrate = Const.toInt(XMLHandler.getTagValue(entrynode, "compressionrate"), -1);
			ifzipfileexists = Const.toInt(XMLHandler.getTagValue(entrynode, "ifzipfileexists"), -1);
			afterzip        = Const.toInt(XMLHandler.getTagValue(entrynode, "afterzip"), -1);
    		wildcard = XMLHandler.getTagValue(entrynode, "wildcard");
			wildcardexclude = XMLHandler.getTagValue(entrynode, "wildcardexclude");
			sourcedirectory = XMLHandler.getTagValue(entrynode, "sourcedirectory");
			movetodirectory = XMLHandler.getTagValue(entrynode, "movetodirectory");
			addfiletoresult = "Y".equalsIgnoreCase(XMLHandler.getTagValue(entrynode, "addfiletoresult"));	
			isfromprevious = "Y".equalsIgnoreCase(XMLHandler.getTagValue(entrynode, "isfromprevious"));	
			createparentfolder = "Y".equalsIgnoreCase(XMLHandler.getTagValue(entrynode, "createparentfolder"));	
			adddate = "Y".equalsIgnoreCase(XMLHandler.getTagValue(entrynode, "adddate"));	
			addtime = "Y".equalsIgnoreCase(XMLHandler.getTagValue(entrynode, "addtime"));	
			SpecifyFormat = "Y".equalsIgnoreCase(XMLHandler.getTagValue(entrynode, "SpecifyFormat"));	
			date_time_format = XMLHandler.getTagValue(entrynode, "date_time_format");
			createMoveToDirectory = "Y".equalsIgnoreCase(XMLHandler.getTagValue(entrynode, "createMoveToDirectory"));
            includingSubFolders = "Y".equalsIgnoreCase(XMLHandler.getTagValue(entrynode, "include_subfolders"));
		}
		catch(KettleXMLException xe)
		{
			throw new KettleXMLException(Messages.getString("JobEntryZipFile.UnableLoadJobEntryXML"), xe);
		}
	}

	public void loadRep(Repository rep, long id_jobentry, List<DatabaseMeta> databases, List<SlaveServer> slaveServers)
	throws KettleException
    {

	    try
	    {
		super.loadRep(rep, id_jobentry, databases, slaveServers);
			zipFilename = rep.getJobEntryAttributeString(id_jobentry, "zipfilename");
			compressionrate=(int) rep.getJobEntryAttributeInteger(id_jobentry, "compressionrate");
			ifzipfileexists=(int) rep.getJobEntryAttributeInteger(id_jobentry, "ifzipfileexists");
			afterzip=(int) rep.getJobEntryAttributeInteger(id_jobentry, "afterzip");
			wildcard = rep.getJobEntryAttributeString(id_jobentry, "wildcard");
			wildcardexclude = rep.getJobEntryAttributeString(id_jobentry, "wildcardexclude");
			sourcedirectory = rep.getJobEntryAttributeString(id_jobentry, "sourcedirectory");
			movetodirectory = rep.getJobEntryAttributeString(id_jobentry, "movetodirectory");
			addfiletoresult=rep.getJobEntryAttributeBoolean(id_jobentry, "addfiletoresult");
			isfromprevious=rep.getJobEntryAttributeBoolean(id_jobentry, "isfromprevious");
			createparentfolder=rep.getJobEntryAttributeBoolean(id_jobentry, "createparentfolder");
			adddate=rep.getJobEntryAttributeBoolean(id_jobentry, "adddate");
			addtime=rep.getJobEntryAttributeBoolean(id_jobentry, "adddate");
			SpecifyFormat=rep.getJobEntryAttributeBoolean(id_jobentry, "SpecifyFormat");
			date_time_format = rep.getJobEntryAttributeString(id_jobentry, "date_time_format");
			createMoveToDirectory=rep.getJobEntryAttributeBoolean(id_jobentry, "createMoveToDirectory");
            includingSubFolders=rep.getJobEntryAttributeBoolean(id_jobentry, "include_subfolders");
		}
		catch(KettleException dbe)
		{
			throw new KettleException(Messages.getString("JobEntryZipFile.UnableLoadJobEntryRep",""+id_jobentry), dbe);
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
			rep.saveJobEntryAttribute(id_job, getID(), "addfiletoresult", addfiletoresult);
			rep.saveJobEntryAttribute(id_job, getID(), "isfromprevious", isfromprevious);
			rep.saveJobEntryAttribute(id_job, getID(), "createparentfolder", createparentfolder);
			rep.saveJobEntryAttribute(id_job, getID(), "addtime", addtime);
			rep.saveJobEntryAttribute(id_job, getID(), "adddate", adddate);
			rep.saveJobEntryAttribute(id_job, getID(), "SpecifyFormat", SpecifyFormat);
			rep.saveJobEntryAttribute(id_job, getID(), "date_time_format", date_time_format);
			rep.saveJobEntryAttribute(id_job, getID(), "createMoveToDirectory", createMoveToDirectory);
            rep.saveJobEntryAttribute(id_job, getID(), "include_subfolders", includingSubFolders);			
		}
		catch(KettleDatabaseException dbe)
		{
			throw new KettleException(Messages.getString("JobEntryZipFile.UnableSaveJobEntryRep",""+id_job), dbe);
		}
	}

	private boolean createParentFolder(String filename)
	{
		// Check for parent folder
		FileObject parentfolder=null;
		
		boolean result=false;
		LogWriter log = LogWriter.getInstance();
		try
		{
			// Get parent folder
			parentfolder=KettleVFS.getFileObject(filename, this).getParent();	
			
    		if(!parentfolder.exists())	
    		{
    			if(log.isDetailed()) log.logDetailed(toString(), Messages.getString("JobEntryZipFile.CanNotFindFolder",""+parentfolder.getName()));
    			parentfolder.createFolder();
    			if(log.isDetailed()) log.logDetailed(toString(), Messages.getString("JobEntryZipFile.FolderCreated",""+parentfolder.getName()));
    		}
    		else
    		{
    			if(log.isDetailed()) log.logDetailed(toString(), Messages.getString("JobEntryZipFile.FolderExists", ""+parentfolder.getName()));
    		}
    		result= true;
		}
		catch (Exception e) {
			log.logError(toString(),Messages.getString("JobEntryZipFile.CanNotCreateFolder",""+parentfolder.getName()));			
		}
		finally {
         	if ( parentfolder != null )
         	{
         		try  {
         			parentfolder.close();
         			parentfolder=null;
         		}
         		catch ( Exception ex ) {};
         	}
        }	
		return result;
	}
	
    public boolean processRowFile(Job parentJob, Result result,String realZipfilename,
        String realWildcard, String realWildcardExclude, String realSourceDirectoryOrFile,
        String realMoveToDirectory, boolean createParentFolder)
    {
        LogWriter log = LogWriter.getInstance();
        boolean fileExists=false;
        File tempFile = null;
        File fileZip =null;
        boolean resultat=false;     
        boolean renameOk = false;   
        boolean orgineExist=false;
        
        // Check if target file/folder exists!
        FileObject originFile = null;
        ZipInputStream zin=null;                    
        byte[] buffer = null;
        FileOutputStream dest = null;
        BufferedOutputStream buff = null;
        ZipOutputStream out = null;
        ZipEntry entry =null;
        String localSourceFilename=realSourceDirectoryOrFile;
        
        try
        {
            originFile = KettleVFS.getFileObject(realSourceDirectoryOrFile, this);
            localSourceFilename=KettleVFS.getFilename(originFile);
            orgineExist=originFile.exists();
        }catch(Exception e){}finally {if (originFile != null )  {try {originFile.close();}
        catch ( IOException ex ) {};}}
        
        String localRealZipfilename=realZipfilename;
        if (realZipfilename!=null  && orgineExist)
        {
            FileObject fileObject = null;
            try {
                fileObject = KettleVFS.getFileObject(localRealZipfilename, this);
                localRealZipfilename=KettleVFS.getFilename(fileObject);
                // Check if Zip File exists
                if (fileObject.exists())
                {
                    fileExists =true;
                    if(log.isDebug())
                        log.logDebug(toString(), Messages.getString("JobZipFiles.Zip_FileExists1.Label")+ localRealZipfilename 
                                            + Messages.getString("JobZipFiles.Zip_FileExists2.Label"));
                } 
                // Let's see if we need to create parent folder of destination zip filename
                if(createParentFolder){createParentFolder(localRealZipfilename);}                   
    
                // Let's start the process now
                if (ifzipfileexists==3 && fileExists)
                {
                    // the zip file exists and user want to Fail
                    resultat = false;
                }
                else if(ifzipfileexists==2 && fileExists)
                {
                    // the zip file exists and user want to do nothing                  
                    if (addfiletoresult)
                    {
                        // Add file to result files name
                        ResultFile resultFile = new ResultFile(ResultFile.FILE_TYPE_GENERAL , fileObject, parentJob.getJobname(), toString());
                        result.getResultFiles().put(resultFile.getFile().toString(), resultFile);
                    }                   
                    resultat = true;
                }
                else if (afterzip==2 && realMoveToDirectory== null)
                {
                    // After Zip, Move files..User must give a destination Folder
                    resultat = false;
                    log.logError(toString(), Messages.getString("JobZipFiles.AfterZip_No_DestinationFolder_Defined.Label"));
                }               
                else 
                    // After Zip, Move files..User must give a destination Folder
                {
                    // Let's see if we deal with file or folder
                    FileObject[] fileList =null;
                    
                    FileObject sourceFileOrFolder = KettleVFS.getFileObject(localSourceFilename);
                    boolean isSourceDirectory = sourceFileOrFolder.getType().equals(FileType.FOLDER);
                    final Pattern pattern;
                    final Pattern patternexclude;
    
                    if (isSourceDirectory) {
                      // Let's prepare the pattern matcher for performance reasons.
                      // We only do this if the target is a folder !
                      //
                      if (!Const.isEmpty(realWildcard)) pattern = Pattern.compile(realWildcard); else pattern=null;
                      if (!Const.isEmpty(realWildcardExclude)) patternexclude = Pattern.compile(realWildcardExclude); else patternexclude=null;
    
                      // Target is a directory
                      // Get all the files in the directory...
                      //
                      if (includingSubFolders) {
                        fileList = sourceFileOrFolder.findFiles(new FileSelector() {
                          
                          public boolean traverseDescendents(FileSelectInfo fileInfo) throws Exception {
                            return true;
                          }
                          
                          public boolean includeFile(FileSelectInfo fileInfo) throws Exception {
                            boolean include;
                            
                            // Only include files in the sub-folders...
                            // When we include sub-folders we match the whole filename, not just the base-name
                            //
                            if (fileInfo.getFile().getType().equals(FileType.FILE)) {
                              include=true;
                              if (pattern!=null) {
                                String name = fileInfo.getFile().getName().getPath();
                                include = pattern.matcher(name).matches();
                              }
                              if (include && patternexclude!=null) {
                                String name = fileInfo.getFile().getName().getPath();
                                include = !pattern.matcher(name).matches();
                              }
                            } else {
                              include=false;
                            }
                            return include;
                          }
                        });
                      } else {
                        fileList = sourceFileOrFolder.getChildren();
                      }
                    } else {
                      pattern=null;
                      patternexclude=null;
                      
                      // Target is a file
                      fileList = new FileObject[] { sourceFileOrFolder };
                    }
                    if(fileList.length==0)
                    {
                        resultat=false;
                        log.logError(toString(), Messages.getString("JobZipFiles.Log.FolderIsEmpty",
                                localSourceFilename));
                    } else if (!checkContainsFile(localSourceFilename, fileList, isSourceDirectory))
                    {
                        resultat=false;
                        log.logError(toString(), Messages.getString("JobZipFiles.Log.NoFilesInFolder",
                                localSourceFilename));
                    }else{
                        if(ifzipfileexists==0 && fileExists)
                        {
                            // the zip file exists and user want to create new one with unique name
                            //Format Date
                            
                            //do we have already a .zip at the end?
                            if (localRealZipfilename.toLowerCase().endsWith(".zip")) {
                                //strip this off
                                localRealZipfilename = localRealZipfilename.substring(0, localRealZipfilename.length()-4);
                            }
                            
                            localRealZipfilename+= "_" + StringUtil.getFormattedDateTimeNow(true) +".zip";      
                            if(log.isDebug())
                                log.logDebug(toString(), Messages.getString("JobZipFiles.Zip_FileNameChange1.Label") + localRealZipfilename + 
                                                    Messages.getString("JobZipFiles.Zip_FileNameChange1.Label"));
                        }
                        else if(ifzipfileexists==1 && fileExists)
                        {
                            // the zip file exists and user want to append                      
                            // get a temp file
                            fileZip = new File(localRealZipfilename);
                            tempFile = File.createTempFile(fileZip.getName(), null);
                            
                            // delete it, otherwise we cannot rename existing zip to it.
                            tempFile.delete();
                            
                            renameOk=fileZip.renameTo(tempFile);
                            
                            if (!renameOk)
                            {
                                log.logError(toString(), Messages.getString("JobZipFiles.Cant_Rename_Temp1.Label")+ fileZip.getAbsolutePath() + Messages.getString("JobZipFiles.Cant_Rename_Temp2.Label") 
                                            + tempFile.getAbsolutePath() + Messages.getString("JobZipFiles.Cant_Rename_Temp3.Label"));
                            }                       
                            if (log.isDebug())
                                log.logDebug(toString(), Messages.getString("JobZipFiles.Zip_FileAppend1.Label") + localRealZipfilename + 
                                            Messages.getString("JobZipFiles.Zip_FileAppend2.Label"));
                        }               
                        
                        if (log.isDetailed())    
                            log.logDetailed(toString(), Messages.getString("JobZipFiles.Files_Found1.Label") +fileList.length+ 
                                            Messages.getString("JobZipFiles.Files_Found2.Label") + localSourceFilename + 
                                            Messages.getString("JobZipFiles.Files_Found3.Label"));
                                                
                        // Prepare Zip File
                        buffer = new byte[18024];
                        dest = new FileOutputStream(localRealZipfilename);
                        buff = new BufferedOutputStream(dest);
                        out = new ZipOutputStream(buff);
                        
                        HashSet<String> fileSet = new HashSet<String>() ;
                                            
                        if( renameOk)
                        {
                            // User want to append files to existing Zip file                       
                            // The idea is to rename the existing zip file to a temporary file 
                            // and then adds all entries in the existing zip along with the new files, 
                            // excluding the zip entries that have the same name as one of the new files.
                            
                            zin = new ZipInputStream(new FileInputStream(tempFile));
                            entry = zin.getNextEntry();             
                            
                             while (entry != null) 
                             {
                                    String name = entry.getName();
                                    
                                    if (!fileSet.contains(name))
                                    {
                                        // Add ZIP entry to output stream.
                                        out.putNextEntry(new ZipEntry(name));
                                        // Transfer bytes from the ZIP file to the output file
                                        int len;
                                        while ((len = zin.read(buffer)) > 0) 
                                        {
                                            out.write(buffer, 0, len);
                                        }
                                        
                                        fileSet.add(name);
                                    }
                                    entry = zin.getNextEntry();
                                }
                                // Close the streams        
                                zin.close();
                        }   
                        
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
                        FileObject[] zippedFiles = new FileObject[fileList.length];
                        int fileNum=0;          
    
                        // Get the files in the list...
                        for (int i=0;i<fileList.length && !parentJob.isStopped();i++)
                        {                       
                            boolean getIt = true;
                            boolean getItexclude = false;           
                    
                            // First see if the file matches the regular expression!
                            // ..only if target is a folder !
                            if (isSourceDirectory) {
                              // If we include sub-folders, we match on the whole name, not just the basename
                              // 
                              String filename;
                              if (includingSubFolders) {
                                filename = fileList[i].getName().getPath();
                              } else {
                                filename = fileList[i].getName().getBaseName();
                              }
                              if (pattern != null) {
                                // Matches the base name of the file (backward compatible!)
                                //
                                Matcher matcher = pattern.matcher(filename);
                                getIt = matcher.matches();
                              }
              
                              if (patternexclude != null) {
                                Matcher matcherexclude = patternexclude.matcher(filename);
                                getItexclude = matcherexclude.matches();
                              }
                            }
                            // Get processing File
                            String targetFilename = KettleVFS.getFilename(fileList[i]);
                            if(sourceFileOrFolder.getType().equals(FileType.FILE)) {
                              targetFilename=localSourceFilename;
                            }
                            
                            // Keep using the File construct here to make sure we're referencing local files only
                            // 
                            File file = new File(targetFilename);
    
                            if (getIt && !getItexclude && !file.isDirectory() && !fileSet.contains(targetFilename))
                            {
    
                                // We can add the file to the Zip Archive
                                if(log.isDebug())
                                    log.logDebug(toString(), Messages.getString("JobZipFiles.Add_FilesToZip1.Label")+fileList[i]+
                                            Messages.getString("JobZipFiles.Add_FilesToZip2.Label")+localSourceFilename+
                                            Messages.getString("JobZipFiles.Add_FilesToZip3.Label"));
                                
                                // Associate a file input stream for the current file
                                FileInputStream in = new FileInputStream(targetFilename);                                                           
    
                                // Add ZIP entry to output stream.
                                //
                                String relativeName;
                                String fullName = fileList[i].getName().getPath();
                                String basePath = sourceFileOrFolder.getName().getPath();
                                if (isSourceDirectory) {
                                  if (fullName.startsWith(basePath)) {
                                    relativeName = fullName.substring(basePath.length()+1);
                                  } else {
                                    relativeName = fullName; 
                                  }
                                } else {
                                  relativeName = fileList[i].getName().getBaseName();
                                }
                                out.putNextEntry(new ZipEntry(relativeName));
                                
                                int len;
                                while ((len = in.read(buffer)) > 0)
                                {
                                    out.write(buffer, 0, len);
                                }
                                out.flush();
                                out.closeEntry();
    
                                // Close the current file input stream
                                in.close(); 
    
                                // Get Zipped File
                                zippedFiles[fileNum] = fileList[i];
                                fileNum=fileNum+1;
                            }
                        }                       
                        // Close the ZipOutPutStream
                        out.close();
                        buff.close();
                        dest.close();
                        
                        if (log.isBasic()) log.logBasic(toString(), Messages.getString("JobZipFiles.Log.TotalZippedFiles", ""+zippedFiles.length));
                        // Delete Temp File
                        if (tempFile !=null)
                        {
                            tempFile.delete();
                        }
                        
                        //-----Get the list of Zipped Files and Move or Delete Them
                        if (afterzip == 1 || afterzip == 2)
                        {   
                            // iterate through the array of Zipped files
                            for (int i = 0; i < zippedFiles.length; i++) 
                            {
                                if ( zippedFiles[i] != null)
                                {                               
                                    // Delete File
                                    FileObject fileObjectd = zippedFiles[i];
                                    if(sourceFileOrFolder.getType().equals(FileType.FILE)) {
                                      fileObjectd = KettleVFS.getFileObject(localSourceFilename, this);     
                                    }
                                    
                                    // Here we can move, delete files
                                    if (afterzip == 1)
                                    {                       
                                        // Delete File
                                        boolean deleted = fileObjectd.delete();
                                        if (!deleted )
                                        {   
                                            resultat = false;
                                            log.logError(toString(), Messages.getString("JobZipFiles.Cant_Delete_File1.Label")+
                                                    localSourceFilename+Const.FILE_SEPARATOR+zippedFiles[i]+
                                                    Messages.getString("JobZipFiles.Cant_Delete_File2.Label"));
    
                                        }
                                        // File deleted
                                        if(log.isDebug())
                                            log.logDebug(toString(), Messages.getString("JobZipFiles.File_Deleted1.Label") + 
                                                    localSourceFilename+Const.FILE_SEPARATOR+zippedFiles[i] + 
                                            Messages.getString("JobZipFiles.File_Deleted2.Label"));
                                    }
                                    else if(afterzip == 2)
                                    {
                                        // Move File    
                                        try
                                        {
                                            FileObject fileObjectm = KettleVFS.getFileObject(realMoveToDirectory + Const.FILE_SEPARATOR+zippedFiles[i], this);
                                            fileObjectd.moveTo(fileObjectm);
                                        }
                                        catch (IOException e) 
                                        {
                                            log.logError(toString(), Messages.getString("JobZipFiles.Cant_Move_File1.Label") +zippedFiles[i]+
                                                Messages.getString("JobZipFiles.Cant_Move_File2.Label") + e.getMessage());
                                            resultat = false;
                                        }
                                        // File moved
                                        if (log.isDebug())
                                            log.logDebug(toString(), Messages.getString("JobZipFiles.File_Moved1.Label") + zippedFiles[i] + 
                                                    Messages.getString("JobZipFiles.File_Moved2.Label"));
                                     }
                                }
                            }
                        }
                                            
                        if (addfiletoresult)
                        {
                            // Add file to result files name
                            ResultFile resultFile = new ResultFile(ResultFile.FILE_TYPE_GENERAL , fileObject, parentJob.getJobname(), toString());
                            result.getResultFiles().put(resultFile.getFile().toString(), resultFile);
                        }
                        
                        resultat = true;
                    }
                }
            }
            catch (Exception e) 
            {
                log.logError(toString(), Messages.getString("JobZipFiles.Cant_CreateZipFile1.Label") +localRealZipfilename+
                                         Messages.getString("JobZipFiles.Cant_CreateZipFile2.Label") + e.getMessage());
                resultat = false;
            }
            finally 
            {
                if ( fileObject != null )
                {
                    try  {fileObject.close();fileObject=null;}
                    catch ( IOException ex ) {};
                }
    
                try{
                    if(out!=null) out.close();
                    if(buff!=null) buff.close();
                    if(dest!=null) dest.close();
                    if(zin!=null) zin.close();
                    if(entry!=null) entry=null;
                    
                }catch ( IOException ex ) {};
            }
        }
        else
        {   
            resultat=true;
            if (localRealZipfilename==null) log.logError(toString(), Messages.getString("JobZipFiles.No_ZipFile_Defined.Label"));
            if (!orgineExist) log.logError(toString(), Messages.getString("JobZipFiles.No_FolderCible_Defined.Label",localSourceFilename));            
        }           
        //return  a verifier
        return resultat;
    }

    private boolean checkContainsFile(String realSourceDirectoryOrFile, FileObject[]  filelist, boolean isDirectory) throws FileSystemException
    {
        boolean retval=false;
        for (int i=0;i<filelist.length;i++){
            FileObject file = filelist[i];
            if((file.exists() && file.getType().equals(FileType.FILE))) retval=true;
        }
        return retval;
    
    }
	
	public Result execute(Result previousResult, int nr, Repository rep, Job parentJob)
	{
		LogWriter log = LogWriter.getInstance();
		Result result = previousResult;
		List<RowMetaAndData> rows = result.getRows();
		
		// reset values
		String realZipfilename       = null;
		String realWildcard          = null;
		String realWildcardExclude   = null;
		String realTargetdirectory   = null;
		String realMovetodirectory   = environmentSubstitute(movetodirectory);
		
		// Sanity check
		boolean SanityControlOK=true;
		
		if(afterzip==2)
		{
			 if(Const.isEmpty(realMovetodirectory))
			 {
				 SanityControlOK=false;
				 log.logError(toString(), Messages.getString("JobZipFiles.AfterZip_No_DestinationFolder_Defined.Label"));
			 }else
			 {
				 FileObject moveToDirectory=null;
				 try{
					 moveToDirectory=KettleVFS.getFileObject(realMovetodirectory, this);
					 if(moveToDirectory.exists())
					 {
						 if(moveToDirectory.getType()==FileType.FOLDER)
						 {
							 if(log.isDetailed()) log.logDetailed(toString(), Messages.getString("JobZipFiles.Log.MoveToFolderExist",realMovetodirectory));
						 }else
						 {
							 SanityControlOK=false; 
							 log.logError(toString(), Messages.getString("JobZipFiles.Log.MoveToFolderNotFolder",realMovetodirectory));
						 }
					 }else
					 {
						 if(log.isDetailed()) log.logDetailed(toString(), Messages.getString("JobZipFiles.Log.MoveToFolderNotNotExist",realMovetodirectory));
						 if(createMoveToDirectory)
						 {
							 moveToDirectory.createFolder();
							 if(log.isDetailed()) log.logDetailed(toString(), Messages.getString("JobZipFiles.Log.MoveToFolderCreaterd",realMovetodirectory));
						 }else
						 {
							 SanityControlOK=false; 
							 log.logError(toString(), Messages.getString("JobZipFiles.Log.MoveToFolderNotNotExist",realMovetodirectory));
						 }
					 }
				 }catch(Exception e)
				 {
					 SanityControlOK=false;
					 log.logError(toString(), Messages.getString("JobZipFiles.ErrorGettingMoveToFolder.Label",realMovetodirectory));
				 }finally
				 {
					 if(moveToDirectory!=null)
					 {
						 realMovetodirectory=KettleVFS.getFilename(moveToDirectory);
						 try
						 { 
							 moveToDirectory.close(); 
							 moveToDirectory=null;
						 }							 
						 catch(Exception e){};
					 }
				}
			 }
		}
		
		if(!SanityControlOK)
		{
			result.setNrErrors(1);
			result.setResult(false);
			return result;
		}
		
		// arguments from previous
		
		if (isfromprevious)
		{
		    if(log.isDetailed()) log.logDetailed(toString(), Messages.getString("JobZipFiles.ArgFromPrevious.Found",(rows!=null?rows.size():0)+ ""));			
		}
		if (isfromprevious && rows!=null)
		{
			try{
				for (int iteration=0;iteration<rows.size() && !parentJob.isStopped();iteration++) 
				{
					// get arguments from previous job entry
					RowMetaAndData resultRow = rows.get(iteration);
					// get target directory
					realTargetdirectory = resultRow.getString(0,null);
					if (!Const.isEmpty(realTargetdirectory))
					{
						//get wildcard to include
						if (!Const.isEmpty(resultRow.getString(1,null)))
							realWildcard = resultRow.getString(1,null);	
						// get wildcard to exclude
						if (!Const.isEmpty(resultRow.getString(2,null)))
							realWildcardExclude = resultRow.getString(2,null);
						
						// get destination zip file
						realZipfilename = resultRow.getString(3,null);
						if (!Const.isEmpty(realZipfilename))
						{
							if (!processRowFile(parentJob,result,realZipfilename,realWildcard,realWildcardExclude, realTargetdirectory, realMovetodirectory,createparentfolder))
							{
								result.setResult(false);
								return result;
							}
						}	
						else
						{
							log.logError(toString(),"destination zip filename is empty! Ignoring row...");
						}
					}
					else
					{
						log.logError(toString(),"Target directory is empty! Ignoring row...");
					}
				}	
			}catch(Exception e){log.logError(toString(),"Erreur during process!");}
		}
		else if (!isfromprevious)
		{
			if(!Const.isEmpty(sourcedirectory))
			{
				// get values from job entry
				realZipfilename       = getFullFilename(environmentSubstitute(zipFilename),adddate, addtime,SpecifyFormat,date_time_format);
				realWildcard          = environmentSubstitute(wildcard);
				realWildcardExclude   = environmentSubstitute(wildcardexclude);	
				realTargetdirectory   = environmentSubstitute(sourcedirectory);
				
				result.setResult(processRowFile(parentJob,result,realZipfilename,realWildcard,realWildcardExclude, realTargetdirectory, realMovetodirectory, createparentfolder));			
			}
			else
			{
				log.logError(toString(),"Source folder/file is empty! Ignoring row...");
			}
		}
	
		// End		
		return result;
	}
	public String getFullFilename(String filename,boolean add_date,boolean add_time, boolean specify_format,
			String datetime_folder)
	{
		String retval="";
		if(Const.isEmpty(filename)) return null;
		
		// Replace possible environment variables...
		String realfilename=environmentSubstitute(filename);
		int lenstring=realfilename.length();
		int lastindexOfDot=realfilename.lastIndexOf('.');
		if(lastindexOfDot==-1) lastindexOfDot=lenstring;
		
		retval=realfilename.substring(0, lastindexOfDot);
		
		final SimpleDateFormat daf     = new SimpleDateFormat();
		Date now = new Date();
		
		if(specify_format && !Const.isEmpty(datetime_folder))
		{
			daf.applyPattern(datetime_folder);
			String dt = daf.format(now);
			retval+=dt;
		}else
		{
			if (add_date)
			{
				daf.applyPattern("yyyyMMdd");
				String d = daf.format(now);
				retval+="_"+d;
			}
			if (add_time)
			{
				daf.applyPattern("HHmmssSSS");
				String t = daf.format(now);
				retval+="_"+t;
			}
		}
		retval+=realfilename.substring(lastindexOfDot, lenstring);
		return retval;

	}
	public boolean evaluates()
	{
		return true;
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
	public boolean isCreateMoveToDirectory()
    {
    	return createMoveToDirectory;
    }
    public void setCreateMoveToDirectory(boolean createMoveToDirectory)
    {
    	this.createMoveToDirectory=createMoveToDirectory;
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
	
	public void setcreateparentfolder(boolean createparentfolder) 
	{
		this.createparentfolder= createparentfolder;
	}
	
	public void setDateInFilename(boolean adddate) 
	{
		this.adddate= adddate;
	}
	
	public boolean isDateInFilename() 
	{
		return adddate;
	}
	public void setTimeInFilename(boolean addtime) 
	{
		this.addtime= addtime;
	}
	public boolean isTimeInFilename() 
	{
		return addtime;
	}
	 public boolean  isSpecifyFormat()
	 {
	   	return SpecifyFormat;
	 }
	 public void setSpecifyFormat(boolean SpecifyFormat)
	 {
	   	this.SpecifyFormat=SpecifyFormat;
	 }
	 public String getDateTimeFormat()
	 {
		return date_time_format;
	 }
	 public void setDateTimeFormat(String date_time_format)
	 {
		this.date_time_format=date_time_format;
	 }
	
	public boolean getcreateparentfolder() 
	{
		return createparentfolder;
	}
	public void setDatafromprevious(boolean isfromprevious) 
	{
		this.isfromprevious = isfromprevious;
	}
	
	public boolean getDatafromprevious() 
	{
		return isfromprevious;
	}	
	
	@Override
	public void check(List<CheckResultInterface> remarks, JobMeta jobMeta)
	{
	    ValidatorContext ctx1 = new ValidatorContext();
	    putVariableSpace(ctx1, getVariables());
	    putValidators(ctx1, notBlankValidator(), fileDoesNotExistValidator());
	    if (3 == ifzipfileexists) {
	      // execute method fails if the file already exists; we should too
	      putFailIfExists(ctx1, true);
	    }
	    andValidator().validate(this, "zipFilename", remarks, ctx1);//$NON-NLS-1$

	    if (2 == afterzip) {
	      // setting says to move
	      andValidator().validate(this, "moveToDirectory", remarks, putValidators(notBlankValidator())); //$NON-NLS-1$
	    }

	    andValidator().validate(this, "sourceDirectory", remarks, putValidators(notBlankValidator())); //$NON-NLS-1$

	}

	  /**
	   * @return true if the search for files to zip in a folder include sub-folders
	   */
	  public boolean isIncludingSubFolders() {
	    return includingSubFolders;
	  }

	  /**
	   * @param includesSubFolders Set to true if the search for files to zip in a folder needs to include sub-folders
	   */
	  public void setIncludingSubFolders(boolean includesSubFolders) {
	    this.includingSubFolders = includesSubFolders;
	  }
}