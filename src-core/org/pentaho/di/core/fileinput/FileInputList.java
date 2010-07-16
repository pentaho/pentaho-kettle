/*
 * Copyright (c) 2007 Pentaho Corporation.  All rights reserved. 
 * This software was developed by Pentaho Corporation and is provided under the terms 
 * of the GNU Lesser General Public License, Version 2.1. You may not use 
 * this file except in compliance with the license. If you need a copy of the license, 
 * please go to http://www.gnu.org/licenses/lgpl-2.1.txt. The Original Code is Pentaho 
 * Data Integration.  The Initial Developer is Pentaho Corporation.
 *
 * Software distributed under the GNU Lesser Public License is distributed on an "AS IS" 
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or  implied. Please refer to 
 * the license for the specific language governing your rights and limitations.
*/
package org.pentaho.di.core.fileinput;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Pattern;

import org.apache.commons.vfs.AllFileSelector;
import org.apache.commons.vfs.FileObject;
import org.apache.commons.vfs.FileSelectInfo;
import org.apache.commons.vfs.FileSystemException;
import org.apache.commons.vfs.FileType;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.logging.LogChannel;
import org.pentaho.di.core.logging.LogChannelInterface;
import org.pentaho.di.core.variables.VariableSpace;
import org.pentaho.di.core.vfs.KettleVFS;

public class FileInputList
{
    private List<FileObject>    files              = new ArrayList<FileObject>();
    private List<FileObject>    nonExistantFiles   = new ArrayList<FileObject>(1);
    private List<FileObject>    nonAccessibleFiles = new ArrayList<FileObject>(1);
    
    private static LogChannelInterface log = new LogChannel("FileInputList");
    
    public enum FileTypeFilter
    {
        FILES_AND_FOLDERS("all_files", FileType.FILE, FileType.FOLDER),
        ONLY_FILES("only_files", FileType.FILE),
        ONLY_FOLDERS("only_folders", FileType.FOLDER);
        
        private String name;
        private final Collection<FileType> allowedFileTypes;
        private FileTypeFilter(String name, FileType... allowedFileTypes)
        {
            this.name = name;
            this.allowedFileTypes = Collections.unmodifiableCollection(Arrays.asList(allowedFileTypes));
        }
        public boolean isFileTypeAllowed(FileType fileType)
        {
            return allowedFileTypes.contains(fileType);
        }
        public String toString()
        {
            return name;
        }
        public static FileTypeFilter getByOrdinal(int ordinal)
        {
            for (FileTypeFilter filter : FileTypeFilter.values())
            {
                if (filter.ordinal() == ordinal)
                {
                    return filter;
                }
            }
            return ONLY_FILES;
        }
        public static FileTypeFilter getByName(String name)
        {
            for (FileTypeFilter filter : FileTypeFilter.values())
            {
                if (filter.name.equals(name))
                {
                    return filter;
                }
            }
            return ONLY_FILES;
        }
    }
    
    private static final String YES                = "Y";

    public static String getRequiredFilesDescription(List<FileObject> nonExistantFiles)
    {
        StringBuffer buffer = new StringBuffer();
        for (Iterator<FileObject> iter = nonExistantFiles.iterator(); iter.hasNext();)
        {
            FileObject file = iter.next();
            buffer.append(file.getName().getURI());
            buffer.append(Const.CR);
        }
        return buffer.toString();
    }

    private static boolean[] includeSubdirsFalse(int iLength)
    {
        boolean[] includeSubdirs = new boolean[iLength];
        for (int i = 0; i < iLength; i++)
            includeSubdirs[i] = false;
        return includeSubdirs;
    }
    
    public static String[] createFilePathList(VariableSpace space, String[] fileName, String[] fileMask, String[] excludeFileMask, String[] fileRequired)
    {
        boolean[] includeSubdirs = includeSubdirsFalse(fileName.length);
        return createFilePathList(space, fileName, fileMask, excludeFileMask, fileRequired, includeSubdirs, null);
    }
    
    public static String[] createFilePathList(VariableSpace space, String[] fileName, String[] fileMask, String[] excludeFileMask, String[] fileRequired,
        boolean[] includeSubdirs)
    {
        return createFilePathList(space, fileName, fileMask, excludeFileMask, fileRequired, includeSubdirs, null);
    }

    public static String[] createFilePathList(VariableSpace space, String[] fileName, String[] fileMask, 
    		 String[] excludeFileMask, String[] fileRequired, boolean[] includeSubdirs, FileTypeFilter[] filters)
    {
    	List<FileObject> fileList = createFileList( space, fileName, fileMask, excludeFileMask, fileRequired, includeSubdirs, filters ).getFiles();
        String[] filePaths = new String[fileList.size()];
        for (int i = 0; i < filePaths.length; i++)
        {
            filePaths[i] = fileList.get(i).getName().getURI();
        }
        return filePaths;
    }

    public static FileInputList createFileList(VariableSpace space, String[] fileName, String[] fileMask, String[] excludeFileMask, String[] fileRequired)
    {
        boolean[] includeSubdirs = includeSubdirsFalse(fileName.length);
        return createFileList(space, fileName, fileMask, excludeFileMask, fileRequired, includeSubdirs, null);
    }
    
    public static FileInputList createFileList(VariableSpace space, String[] fileName, String[] fileMask, String[] excludeFileMask, String[] fileRequired, boolean[] includeSubdirs)
    {
        return createFileList(space, fileName, fileMask, excludeFileMask, fileRequired, includeSubdirs, null);
    }

    public static FileInputList createFileList(VariableSpace space, String[] fileName, String[] fileMask, 
    		String[] excludeFileMask, String[] fileRequired, boolean[] includeSubdirs, 
    		FileTypeFilter[] fileTypeFilters)
    {
        FileInputList fileInputList = new FileInputList();

        // Replace possible environment variables...
        final String realfile[] = space.environmentSubstitute(fileName);
        final String realmask[] = space.environmentSubstitute(fileMask);
        final String realExcludeMask[] = space.environmentSubstitute(excludeFileMask);

        for (int i = 0; i < realfile.length; i++)
        {
            final String onefile = realfile[i];
            final String onemask = realmask[i];
            final String excludeonemask = realExcludeMask[i];
            final boolean onerequired = YES.equalsIgnoreCase(fileRequired[i]);
            final boolean subdirs = includeSubdirs[i];
            final FileTypeFilter filter = (
                    (fileTypeFilters == null || fileTypeFilters[i] == null) ?
                            FileTypeFilter.ONLY_FILES : fileTypeFilters[i]);
            
            if (Const.isEmpty(onefile)) continue;

            // 
            // If a wildcard is set we search for files
            //
            if (!Const.isEmpty(onemask) || !Const.isEmpty(excludeonemask))
            {
                try
                {
                	  FileObject directoryFileObject = KettleVFS.getFileObject(onefile, space);
                      boolean processFolder=true;
                      if(onerequired)
                      {
                      	if(!directoryFileObject.exists()) 
                      	{
                      		// if we don't find folder..no need to continue
                      		fileInputList.addNonExistantFile(directoryFileObject);
                      		processFolder=false;
                      	}
                      	else
                      	{
                      		if(!directoryFileObject.isReadable())
                      		{
                      			fileInputList.addNonAccessibleFile(directoryFileObject);	
                      			processFolder=false;
                      		}
                      	}
                      }
                      
                    // Find all file names that match the wildcard in this directory
                    //
                      if(processFolder)
                      {
	                    if (directoryFileObject != null && directoryFileObject.getType() == FileType.FOLDER) // it's a directory
	                    {
	                        FileObject[] fileObjects = directoryFileObject.findFiles(
	                                new AllFileSelector()
	                                {
	                                    public boolean traverseDescendents(FileSelectInfo info)
	                                    {
	                                        return info.getDepth()==0 || subdirs;
	                                    }
	                                    
	                                    public boolean includeFile(FileSelectInfo info)
	                                    {
	                                        // Never return the parent directory of a file list.
	                                        if (info.getDepth() == 0) {
	                                            return false;
	                                        }
	                                        
	                                    	FileObject fileObject = info.getFile();
	                                    	try {
	                                    	    if ( fileObject != null && filter.isFileTypeAllowed(fileObject.getType()))
	                                    	    {
	                                    	    	String name = info.getFile().getName().getBaseName();
	    	                                    	boolean matches=true;
	    	                                        if(!Const.isEmpty(onemask)) {
	    	                                           	matches = Pattern.matches(onemask, name);
	    	                                        }
	    	                                    	boolean excludematches=false;
	    	                                        if(!Const.isEmpty(excludeonemask)) {
	    	                                        	excludematches = Pattern.matches(excludeonemask, name);
	    	                                        }
	    	                                        return (matches && !excludematches);
	                                    	    }
	                                    	    return false;
	                                    	}
	                                    	catch ( FileSystemException ex )
	                                    	{
	                                    		// Upon error don't process the file.
	                                    		return false;
	                                    	}
	                                    }
	                                }
	                            );
	                        if (fileObjects != null) 
	                        {
	                            for (int j = 0; j < fileObjects.length; j++)
	                            {
	                                if (fileObjects[j].exists()) fileInputList.addFile(fileObjects[j]);
	                            }
	                        }
	                        if (Const.isEmpty(fileObjects))
	                        {
	                            if (onerequired) fileInputList.addNonAccessibleFile(directoryFileObject);
	                        }
	                        
	                        // Sort the list: quicksort, only for regular files
	                        fileInputList.sortFiles();
	                    }
	                    else
	                    {
	                        FileObject[] children = directoryFileObject.getChildren();
	                        for (int j = 0; j < children.length; j++)
	                        {
	                            // See if the wildcard (regexp) matches...
	                            String name = children[j].getName().getBaseName();
	                            boolean matches=true;
                                if(!Const.isEmpty(onemask)) {
                                   	matches = Pattern.matches(onemask, name);
                                }
                            	boolean excludematches=false;
                                if(!Const.isEmpty(excludeonemask)) {
                                	excludematches = Pattern.matches(excludeonemask, name);
                                }
                                if(matches && !excludematches){
                                	fileInputList.addFile(children[j]);
                                }
	                            
	                            
	                        }
	                        // We don't sort here, keep the order of the files in the archive.
	                    }
                    }
                }
                catch (Exception e)
                {
                    log.logError(Const.getStackTracker(e));
                }
            }
            else
            // A normal file...
            {
                try
                {
                    FileObject fileObject = KettleVFS.getFileObject(onefile, space);
                    if (fileObject.exists())
                    {
                        if (fileObject.isReadable())
                        {
                            fileInputList.addFile(fileObject);
                        }
                        else
                        {
                            if (onerequired) fileInputList.addNonAccessibleFile(fileObject);
                        }
                    }
                    else
                    {
                        if (onerequired) fileInputList.addNonExistantFile(fileObject);
                    }
                }
                catch (Exception e)
                {
                	log.logError(Const.getStackTracker(e));
                }
            }
        }


        return fileInputList;
    }
    public static FileInputList createFolderList(VariableSpace space, String[] folderName, String[] folderRequired)
    {
        FileInputList fileInputList = new FileInputList();

        // Replace possible environment variables...
        final String realfolder[] = space.environmentSubstitute(folderName);

        for (int i = 0; i < realfolder.length; i++)
        {
            final String onefile = realfolder[i];
            final boolean onerequired = YES.equalsIgnoreCase(folderRequired[i]);
            final boolean subdirs = true;
            final FileTypeFilter filter = FileTypeFilter.ONLY_FOLDERS;
            
            if (Const.isEmpty(onefile)) continue;
            FileObject directoryFileObject=null;
 
	        try
	        {
	            // Find all folder names  in this directory
	            //
	            directoryFileObject = KettleVFS.getFileObject(onefile, space);
	            if (directoryFileObject != null && directoryFileObject.getType() == FileType.FOLDER) // it's a directory
	            {
	            	
	                FileObject[] fileObjects = directoryFileObject.findFiles(
	                        new AllFileSelector()
	                        {
	                            public boolean traverseDescendents(FileSelectInfo info)
	                            {
	                                return info.getDepth()==0 || subdirs;
	                            }
	                            
	                            public boolean includeFile(FileSelectInfo info)
	                            {
	                                // Never return the parent directory of a file list.
	                                if (info.getDepth() == 0) {
	                                    return false;
	                                }
	                                
	                            	FileObject fileObject = info.getFile();
	                            	try {
	                            	    if ( fileObject != null && filter.isFileTypeAllowed(fileObject.getType())){
	                                        return true;
	                            	    }
	                            	    return false;
	                            	}
	                            	catch ( FileSystemException ex ){
	                            		// Upon error don't process the file.
	                            		return false;
	                            	}
	                            }
	                        }
	                    );
	                if (fileObjects != null) {
	                    for (int j = 0; j < fileObjects.length; j++)
	                    {
	                        if (fileObjects[j].exists()) fileInputList.addFile(fileObjects[j]);
	                    }
	                }
	                if (Const.isEmpty(fileObjects))
	                {
	                    if (onerequired) fileInputList.addNonAccessibleFile(directoryFileObject);
	                }
	                
	                // Sort the list: quicksort, only for regular files
	                fileInputList.sortFiles();
	            }else{
	            	if(onerequired && !directoryFileObject.exists()) fileInputList.addNonExistantFile(directoryFileObject);
	            }
	        }
	        catch (Exception e)
	        {
	        	log.logError(Const.getStackTracker(e));
	        }finally{
	        	try{if(directoryFileObject!=null) directoryFileObject.close();directoryFileObject=null;}catch(Exception e){};
	        }
	    }

        return fileInputList;
    }  
    public List<FileObject> getFiles()
    {
        return files;
    }
    
    public String[] getFileStrings()
    {
        String[] fileStrings = new String[files.size()];
        for (int i=0;i<fileStrings.length;i++)
        {
            fileStrings[i] = KettleVFS.getFilename(files.get(i));
        }
        return fileStrings;
    }

    public List<FileObject> getNonAccessibleFiles()
    {
        return nonAccessibleFiles;
    }

    public List<FileObject> getNonExistantFiles()
    {
        return nonExistantFiles;
    }

    public void addFile(FileObject file)
    {
        files.add(file);
    }

    public void addNonAccessibleFile(FileObject file)
    {
        nonAccessibleFiles.add(file);
    }

    public void addNonExistantFile(FileObject file)
    {
        nonExistantFiles.add(file);
    }

    public void sortFiles()
    {
        Collections.sort(files, KettleVFS.getComparator());
        Collections.sort(nonAccessibleFiles, KettleVFS.getComparator());
        Collections.sort(nonExistantFiles, KettleVFS.getComparator());
    }

    /*
    private boolean containsComparable(List list)
    {
        if (list == null || list.size() == 0)
            return false;
        
        return (list.get(0) instanceof Comparable);
    }
    */
    
    public FileObject getFile(int i)
    {
        return files.get(i);
    }

    public int nrOfFiles()
    {
        return files.size();
    }

    public int nrOfMissingFiles()
    {
        return nonAccessibleFiles.size() + nonExistantFiles.size();
    }

    public static FileInputList createFileList(VariableSpace space, String[] fileName, String[] fileMask, String[] fileRequired, boolean[] includeSubdirs)
    {
        return createFileList(space, fileName, fileMask, null, fileRequired, includeSubdirs, null);
    }
    public static String[] createFilePathList(VariableSpace space, String[] fileName, String[] fileMask, String[] fileRequired)
    {
        boolean[] includeSubdirs = includeSubdirsFalse(fileName.length);
        return createFilePathList(space, fileName, fileMask, new String[] { null }, fileRequired, includeSubdirs, null);
    }
}
