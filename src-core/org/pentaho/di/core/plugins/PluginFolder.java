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

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.vfs.FileObject;
import org.apache.commons.vfs.FileSelectInfo;
import org.apache.commons.vfs.FileSelector;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.exception.KettleFileException;
import org.pentaho.di.core.util.EnvUtil;
import org.pentaho.di.core.vfs.KettleVFS;

/**
 * A folder to search plugins in.
 * 
 * @author matt
 *
 */
public class PluginFolder implements PluginFolderInterface {

	private String folder;
	private boolean pluginXmlFolder; 
	private boolean pluginAnnotationsFolder; 
	
	/**
	 * @param folder The folder location
	 * @param pluginXmlFolder set to true if the folder needs to be searched for plugin.xml appearances
	 * @param pluginAnnotationsFolder set to true if the folder needs to be searched for jar files with plugin annotations
	 */
	public PluginFolder(String folder, boolean pluginXmlFolder, boolean pluginAnnotationsFolder) {
		this.folder = folder;
		this.pluginXmlFolder = pluginXmlFolder;
		this.pluginAnnotationsFolder = pluginAnnotationsFolder;
	}
	
	public String toString() {
		return folder;
	}
	
	/**
	 * Create a list of plugin folders based on the specified xml sub folder
	 * @param xmlSubfolder the sub-folder to consider for XML plugin files or null if it's not applicable.
	 * @return The list of plugin folders found
	 */
	public static List<PluginFolderInterface> populateFolders(String xmlSubfolder) {
		List<PluginFolderInterface> pluginFolders = new ArrayList<PluginFolderInterface>();
		String folderPaths = EnvUtil.getSystemProperty("KETTLE_PLUGIN_BASE_FOLDERS");
		if (folderPaths == null) {
			folderPaths = Const.DEFAULT_PLUGIN_BASE_FOLDERS;
		}
		if (folderPaths != null) {
			String folders[] = folderPaths.split(",");
			// for each folder in the list of plugin base folders
			// add an annotation and xml path for searching
			for (String folder : folders) {
				pluginFolders.add(new PluginFolder(folder, false, true));
				if (!Const.isEmpty(xmlSubfolder)) {
			      pluginFolders.add(new PluginFolder(folder + File.separator + xmlSubfolder, true, false));
				}
			}
		}
		return pluginFolders;
	}
	
	public FileObject[] findJarFiles() throws KettleFileException {
		
		try {
			// Find all the jar files in this folder...
			//
			FileObject folderObject = KettleVFS.getFileObject( this.getFolder() );
			FileObject[] fileObjects = folderObject.findFiles(new FileSelector() {
				public boolean traverseDescendents(FileSelectInfo fileSelectInfo) throws Exception {
				  return fileSelectInfo.getDepth() < 2;
				}
				
				public boolean includeFile(FileSelectInfo fileSelectInfo) throws Exception {
					return fileSelectInfo.getFile().toString().matches(".*\\.jar$");
				}
			});
			
			return fileObjects;
		} catch(Exception e) {
			throw new KettleFileException("Unable to list jar files in plugin folder '"+toString()+"'", e);
		}
	}

	/**
	 * @return the folder
	 */
	public String getFolder() {
		return folder;
	}
	
	/**
	 * @param folder the folder to set
	 */
	public void setFolder(String folder) {
		this.folder = folder;
	}

	/**
	 * @return the pluginXmlFolder
	 */
	public boolean isPluginXmlFolder() {
		return pluginXmlFolder;
	}

	/**
	 * @param pluginXmlFolder the pluginXmlFolder to set
	 */
	public void setPluginXmlFolder(boolean pluginXmlFolder) {
		this.pluginXmlFolder = pluginXmlFolder;
	}

	/**
	 * @return the pluginAnnotationsFolder
	 */
	public boolean isPluginAnnotationsFolder() {
		return pluginAnnotationsFolder;
	}

	/**
	 * @param pluginAnnotationsFolder the pluginAnnotationsFolder to set
	 */
	public void setPluginAnnotationsFolder(boolean pluginAnnotationsFolder) {
		this.pluginAnnotationsFolder = pluginAnnotationsFolder;
	}	
}
