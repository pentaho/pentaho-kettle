package org.pentaho.di.core.plugins;

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
