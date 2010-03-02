package org.pentaho.di.core.plugins;

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
}
