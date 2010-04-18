 /* Copyright (c) 2007 Pentaho Corporation.  All rights reserved. 
 * This software was developed by Pentaho Corporation and is provided under the terms 
 * of the GNU Lesser General Public License, Version 2.1. You may not use 
 * this file except in compliance with the license. If you need a copy of the license, 
 * please go to http://www.gnu.org/licenses/lgpl-2.1.txt. The Original Code is Pentaho 
 * Data Integration.  The Initial Developer is Samatar Hassan.
 *
 * Software distributed under the GNU Lesser Public License is distributed on an "AS IS" 
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or  implied. Please refer to 
 * the license for the specific language governing your rights and limitations.*/
 

package org.pentaho.di.job.entries.checkfilelocked;

import org.apache.commons.vfs.FileObject;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.vfs.KettleVFS;


public class LockFile {	
	
	/** name of file to check      **/
	private String filename;
	/** lock indicator             **/
	private boolean locked;
	
	/**
	 * Checks if a file is locked
	 * In order to check is a file is locked
	 * we will use a dummy renaming exercise
	 * @param filename
	 * @throws KettleException
	 */
	public LockFile(String filename) throws KettleException {
		setFilename(filename);
		setLocked(false);
		
		// In order to check is a file is locked
		// we will use a dummy renaming exercise		
		FileObject file=null;
		FileObject dummyfile=null;
		
		try {
			
			file= KettleVFS.getFileObject(filename);
			if(file.exists()) {
				dummyfile= KettleVFS.getFileObject(filename);
				// move file to itself!
				file.moveTo(dummyfile);
			}
		} catch(Exception e){
			// We got an exception
			// The is locked by another process
			setLocked(true);
		} finally {
			if(file!=null) try{ file.close();}catch(Exception e){};
			if(dummyfile!=null) try{ file.close();}catch(Exception e){};
		}

	}

	/**
	 *  Returns filename
	 * @return filename
	 */
	public String getFilename(){
		return this.filename;
	}
	
	/**
	 * Set filename
	 * @param filename
	 */
	private void setFilename(String filename) {
		this.filename= filename;
	}
	
	/**
	 * Returns lock indicator
	 * @return TRUE is file is locked
	 */
	public boolean isLocked() {
		return this.locked;
	}
	
	/**
	 * Set lock indicator
	 * @param lock
	 */
	private void setLocked(boolean lock) {
		this.locked= lock;
	}
}