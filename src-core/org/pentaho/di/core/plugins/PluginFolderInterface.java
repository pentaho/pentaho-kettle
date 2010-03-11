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
