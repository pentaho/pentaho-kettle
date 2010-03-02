package org.pentaho.di.core.plugins;

import java.util.List;

/**
 * This interface describes a plugin type.<br>
 * It expresses the ID and the name of the plugin type.<br>
 * Then it also explains what the plugin meta class is called and classes the plugin interface itself.<br>
 * It also explains us where to load plugins of this type.<br>
 * 
 * @author matt
 *
 */
public interface PluginTypeInterface {
	/**
	 * @return The ID of this plugin type
	 */
	public String getId();
	
	/**
	 * @return The name of this plugin
	 */
	public String getName();
	
	/**
	 * @return The places where we should look for plugins, both as plugin.xml and as 
	 */
	public List<PluginFolderInterface> getPluginFolders();
	
	/**
	 * 
	 * @throws KettlePluginException
	 */
	public void searchPlugins() throws KettlePluginException;
	
	/**
	 * @return The natural order in which the categories appear in the UI
	 */
	public String[] getNaturalCategoriesOrder();
}
