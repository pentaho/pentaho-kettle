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
package org.pentaho.di.resource;

import java.util.Map;

import org.apache.commons.vfs.FileObject;
import org.apache.commons.vfs.FileSystemException;
import org.pentaho.di.core.variables.VariableSpace;

public interface ResourceNamingInterface {
	
	public static enum FileNamingType  {
		TRANSFORMATION,
		JOB,
		DATA_FILE,
		SHELL_SCRIPT,
	}
	
	/**
	 * Create a (file) name for a resource based on a prefix and an extension.
	 * @param prefix The prefix, usually the name of the object that is being exported
     * @param originalFilePath The original path to the file. This will be used in the naming of the resource to ensure that the same GUID will be returned for the same file.
	 * @param extension The extension of the filename to be created.  For now this also gives a clue as to what kind of data is being exported and named..
	 * @param namingType the file naming type to use, in case of DATA_FILE for example, the return value might not be the complete file, but rather 
	 * @return The filename, typically including a GUID, but always the same when given the same prefix and extension as input.
	 */
	public String nameResource(String prefix, String originalFilePath, String extension, FileNamingType namingType);
	
	/**
     * Create a (file) name based on the passed FileObject
     * @param FileObject fileObject The file in which the name ....
     * @param VariableSpace variable(space) of the transformation or job.
     * @param pathOnly Set to true to just return the path, false to return file name and path
     * @return String The file name with the path set as a variable. If pathOnly is set to true then the file name will be left out.
     */
    public String nameResource(FileObject fileObject, VariableSpace space, boolean pathOnly) throws FileSystemException;

	
	/**
	 * @return the map of folders mapped to created parameters during the resource naming.
	 */
	public Map<String, String> getDirectoryMap();
}
