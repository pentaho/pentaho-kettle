package org.pentaho.di.core.plugins;

import java.util.List;
import java.util.Map;

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
   * Register an additional class  type to be managed by the plugin system.
   * @param clz category class, ususally an interface
   * @param xmlNodeName xml node to search for a class name
   */
  public void addObjectType(Class clz, String xmlNodeName);
  
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
	
}
