/*
 * Copyright (c) 2010 Pentaho Corporation.  All rights reserved. 
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
package org.pentaho.di.core.plugins;

import org.apache.commons.vfs.FileObject;
import org.pentaho.di.core.exception.KettleFileException;

/**
 * Describes a possible location for a plugin
 * 
 * @author matt
 *
 */
public interface PluginFolderInterface {
	
	
	/**
	 * @return The folder location
	 */
	public String getFolder();
	
	/**
	 * @return true if the folder needs to be searched for plugin.xml appearances
	 */
	public boolean isPluginXmlFolder();
	
	/**
	 * @return true if the folder needs to be searched for jar files with plugin annotations
	 */
	public boolean isPluginAnnotationsFolder();

	/**
	 * Find all the jar files in this plugin folder
	 * @return The jar files
	 * @throws KettleFileException In case there is a problem reading
	 */
	public FileObject[] findJarFiles() throws KettleFileException;

}
