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
package org.pentaho.di.core.vfs;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.Comparator;

import org.apache.commons.vfs.FileContent;
import org.apache.commons.vfs.FileName;
import org.apache.commons.vfs.FileObject;
import org.apache.commons.vfs.FileSystemException;
import org.apache.commons.vfs.FileSystemManager;
import org.apache.commons.vfs.FileSystemOptions;
import org.apache.commons.vfs.VFS;
import org.apache.commons.vfs.impl.DefaultFileSystemManager;
import org.apache.commons.vfs.provider.local.LocalFile;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.exception.KettleFileException;
import org.pentaho.di.core.util.UUIDUtil;
import org.pentaho.di.core.variables.VariableSpace;
import org.pentaho.di.core.vfs.configuration.IKettleFileSystemConfigBuilder;
import org.pentaho.di.core.vfs.configuration.KettleFileSystemConfigBuilderFactory;
import org.pentaho.di.i18n.BaseMessages;

public class KettleVFS
{
	private static Class<?> PKG = KettleVFS.class; // for i18n purposes, needed by Translator2!!   $NON-NLS-1$

	private static KettleVFS kettleVFS;
  
    private KettleVFS()
    {
    	// Install a shutdown hook to make sure that the file system manager is closed
    	// This will clean up temporary files in vfs_cache
    	//
        Thread thread = new Thread(new Runnable(){
        	public void run() {
		        try
		        {
		            FileSystemManager mgr = VFS.getManager();
		            if (mgr instanceof DefaultFileSystemManager)
		            {
		                ((DefaultFileSystemManager)mgr).close();
		            }
		        }
		        catch (FileSystemException e)
		        {
		            e.printStackTrace();
		        }
	        }
        });
        Runtime.getRuntime().addShutdownHook(thread); 
    }
    
    private synchronized static void checkHook() {
    	if (kettleVFS==null) kettleVFS=new KettleVFS(); 
    }
    
    public static FileObject getFileObject(String vfsFilename) throws KettleFileException {
      return getFileObject(vfsFilename, null);
    }
    
    
    public static FileObject getFileObject(String vfsFilename, VariableSpace space) throws KettleFileException {
      return getFileObject(vfsFilename, space, null);
    }
    
    public static FileObject getFileObject(String vfsFilename, VariableSpace space, FileSystemOptions fsOptions) throws KettleFileException
    {
    	checkHook();
    	
    	try {
	        FileSystemManager fsManager = VFS.getManager();
	        
	        // We have one problem with VFS: if the file is in a subdirectory of the current one: somedir/somefile
	        // In that case, VFS doesn't parse the file correctly.
	        // We need to put file: in front of it to make it work.
	        // However, how are we going to verify this?
	        // 
	        // We are going to see if the filename starts with one of the known protocols like file: zip: ram: smb: jar: etc.
	        // If not, we are going to assume it's a file.
	        //
	        boolean relativeFilename=true;
	        String[] schemes = VFS.getManager().getSchemes();
	        for (int i=0;i<schemes.length && relativeFilename;i++)
	        {
	            if (vfsFilename.startsWith(schemes[i]+":")) { //$NON-NLS-1$
	              relativeFilename=false;
	              // We have a VFS URL, load any options for the file system driver
	              fsOptions = buildFsOptions(space, fsOptions, vfsFilename, schemes[i]);
	            }
	        }
	        
	        String filename;
	        if (vfsFilename.startsWith("\\\\")) //$NON-NLS-1$
	        {
	            File file = new File(vfsFilename);
	            filename = file.toURI().toString();
	        }
	        else
	        {
	            if (relativeFilename)
	            {
	                File file = new File(vfsFilename);
	                filename = file.getAbsolutePath();
	            }
	            else
	            {
	                filename = vfsFilename;
	            }
	        }
	        
	        FileObject fileObject = null;
	        
	        if(fsOptions != null) {
	          fileObject = fsManager.resolveFile(filename, fsOptions);
	        } else {
	          fileObject = fsManager.resolveFile(filename);
	        }
	        
	        return fileObject;
    	}
    	catch(IOException e) {
    		throw new KettleFileException("Unable to get VFS File object for filename '"+vfsFilename+"'", e);
    	}
    }
    
    private static FileSystemOptions buildFsOptions(VariableSpace varSpace, FileSystemOptions sourceOptions, String vfsFilename, String scheme) throws IOException {
      if(varSpace == null || vfsFilename == null) {
        // We cannot extract settings from a non-existant variable space
        return null;
      }
      
      IKettleFileSystemConfigBuilder configBuilder = KettleFileSystemConfigBuilderFactory.getConfigBuilder(varSpace, scheme);
      
      FileSystemOptions fsOptions = (sourceOptions == null) ? new FileSystemOptions() : sourceOptions;

      String[] varList = varSpace.listVariables();
      
      for(String var : varList) {
        if(var.startsWith("vfs.")) { //$NON-NLS-1$
          String param = configBuilder.parseParameterName(var, scheme);
          if(param != null) {
            configBuilder.setParameter(fsOptions, param, varSpace.getVariable(var), var, vfsFilename);
          } else {
            throw new IOException("FileSystemConfigBuilder could not parse parameter: " + var); //$NON-NLS-1$
          }
        }
      }
      return fsOptions;
    }
    
    /**
     * Read a text file (like an XML document).  WARNING DO NOT USE FOR DATA FILES.
     * 
     * @param vfsFilename the filename or URL to read from
     * @param charSetName the character set of the string (UTF-8, ISO8859-1, etc)
     * @return The content of the file as a String
     * @throws IOException
     */
    public static String getTextFileContent(String vfsFilename, String charSetName) throws KettleFileException {
     return getTextFileContent(vfsFilename, null, charSetName);
    }
    
    public static String getTextFileContent(String vfsFilename, VariableSpace space, String charSetName) throws KettleFileException
    {
      	  try {
  	        InputStream inputStream = null; 
        
          if(space == null) {
            inputStream = getInputStream(vfsFilename);
          } else {
            inputStream = getInputStream(vfsFilename, space);
          }
	        InputStreamReader reader = new InputStreamReader(inputStream, charSetName);
	        int c;
	        StringBuffer stringBuffer = new StringBuffer();
	        while ( (c=reader.read())!=-1) stringBuffer.append((char)c);
	        reader.close();
	        inputStream.close();
	        
	        return stringBuffer.toString();
    	} catch(IOException e) {
    		throw new KettleFileException(e);
    	}
    }
    
    public static boolean fileExists(String vfsFilename) throws KettleFileException {
      return fileExists(vfsFilename, null);
    }
    
    public static boolean fileExists(String vfsFilename, VariableSpace space) throws KettleFileException
    {
    	try {
	        FileObject fileObject = getFileObject(vfsFilename, space);
	        return fileObject.exists();
    	} catch(IOException e) {
    		throw new KettleFileException(e);
    	}
    }
    
    public static InputStream getInputStream(FileObject fileObject) throws FileSystemException
    {
        FileContent content = fileObject.getContent();
        return content.getInputStream();
    }
    
    public static InputStream getInputStream(String vfsFilename) throws KettleFileException {
      return getInputStream(vfsFilename, null);
    }
    
    public static InputStream getInputStream(String vfsFilename, VariableSpace space) throws KettleFileException
    {
    	try {
			FileObject fileObject = getFileObject(vfsFilename, space);
			return getInputStream(fileObject);
		} catch (IOException e) {
			throw new KettleFileException(e);
		}
    }
    
    public static OutputStream getOutputStream(FileObject fileObject, boolean append) throws IOException
    {
        FileObject parent = fileObject.getParent();
        if (parent!=null)
        {
            if (!parent.exists())
            {
                throw new IOException(BaseMessages.getString(PKG, "KettleVFS.Exception.ParentDirectoryDoesNotExist", getFilename(parent)));
            }
        }
        try
        {
	        fileObject.createFile();
	        FileContent content = fileObject.getContent();
	        return content.getOutputStream(append);
        }
        catch(FileSystemException e)
        {
        	// Perhaps if it's a local file, we can retry using the standard
        	// File object.  This is because on Windows there is a bug in VFS.
        	//
        	if (fileObject instanceof LocalFile) 
        	{
        		try
        		{
	        		String filename = getFilename(fileObject);
	        		return new FileOutputStream(new File(filename), append);
        		}
        		catch(Exception e2)
        		{
        			throw e; // throw the original exception: hide the retry.
        		}
        	}
        	else
        	{
        		throw e;
        	}
        }
    }
    
    public static OutputStream getOutputStream(String vfsFilename, boolean append) throws KettleFileException {
      return getOutputStream(vfsFilename, null, append);
    }
    
    public static OutputStream getOutputStream(String vfsFilename, VariableSpace space, boolean append) throws KettleFileException
    {
    	try {
	        FileObject fileObject = getFileObject(vfsFilename, space);
	        return getOutputStream(fileObject, append);
    	} catch(IOException e) {
    		throw new KettleFileException(e);
    	}
    }
    
    public static String getFilename(FileObject fileObject)
    {
        FileName fileName = fileObject.getName();
        String root = fileName.getRootURI();
        if (!root.startsWith("file:")) return fileName.getURI(); // nothing we can do about non-normal files. //$NON-NLS-1$
        if (root.endsWith(":/")) // Windows //$NON-NLS-1$
        {
            root = root.substring(8,10);
        }
        else // *nix & OSX
        {
            root = ""; //$NON-NLS-1$
        }
        String fileString = root + fileName.getPath();
        if (!"/".equals(Const.FILE_SEPARATOR)) //$NON-NLS-1$
        {
            fileString = Const.replace(fileString, "/", Const.FILE_SEPARATOR); //$NON-NLS-1$
        }
        return fileString;
    }
    
    public static FileObject createTempFile(String prefix, String suffix, String directory) throws KettleFileException {
      return createTempFile(prefix, suffix, directory, null);
    }
    
    public static FileObject createTempFile(String prefix, String suffix, String directory, VariableSpace space) throws KettleFileException
    {
    	try {
	        FileObject fileObject;
	        do
	        {
	          // Build temporary file name using UUID to ensure uniqueness. Old mechanism would fail using Sort Rows (for example)
	          // when there multiple nodes with multiple JVMs on each node. In this case, the temp file names would end up being
	          // duplicated which would cause the sort to fail.
	          String filename = new StringBuffer(50).append(directory).append('/').append(prefix).append('_').append(UUIDUtil.getUUIDAsString()).append(suffix).toString();
	          fileObject = getFileObject(filename, space);
	        }
	        while (fileObject.exists());
	        return fileObject;
    	} catch(IOException e) {
    		throw new KettleFileException(e);
    	}
    }
    
    public static Comparator<FileObject> getComparator()
    {
        return new Comparator<FileObject>()
        {
            public int compare(FileObject o1, FileObject o2)
            {
                String filename1 = getFilename( o1);
                String filename2 = getFilename( o2);
                return filename1.compareTo(filename2);
            }
        };
    }

    /**
     * Get a FileInputStream for a local file.  Local files can be read with NIO.
     * 
     * @param fileObject
     * @return a FileInputStream
     * @throws IOException
     * @deprecated because of API change in Apache VFS.  As a workaround use FileObject.getName().getPathDecoded();
     * Then use a regular File() object to create a File Input stream.
     */
	public static FileInputStream getFileInputStream(FileObject fileObject) throws IOException {
		
		if (!(fileObject instanceof LocalFile)) {
			// We can only use NIO on local files at the moment, so that's what we limit ourselves to.
			//
			throw new IOException(BaseMessages.getString(PKG, "FixedInput.Log.OnlyLocalFilesAreSupported"));
		}
				
		return new FileInputStream( fileObject.getName().getPathDecoded() );
	}

}
