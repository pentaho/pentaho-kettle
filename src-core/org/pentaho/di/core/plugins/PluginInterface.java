package org.pentaho.di.core.plugins;

import java.util.List;
import java.util.Map;

/**
 * This interface describes the plugin itself, the IDs it listens too, what libraries (jar files) it uses, the names, the i18n detailes, etc.
 * 
 * @author matt
 *
 */
public interface PluginInterface {
	
	/**
	 * @return All the possible IDs that this plugin corresponds with.<br>
	 * Multiple IDs are typically used when you migrate 2 different plugins into a single one with the same functionality.<br>
	 * It can also happen if you deprecate an older plugin and you want to have a new one provide compatibility for it.<br>
	 */
	public String[] getIds();
	
	/**
	 * @return The type of plugin
	 */
	public Class<? extends PluginTypeInterface> getPluginType();
	
	/**
	 * @return The main class assigned to this Plugin. 
	 */
	public Class<?> getMainType();
	
	/**
	 * @return The libraries (jar file names) that are used by this plugin
	 */
	public List<String> getLibraries();
	
	/**
	 * @return The name of the plugin
	 */
	public String getName();
	
	/**
	 * @return The description of the plugin
	 */
	public String getDescription();
	
	/**
	 * @return The location of the image (icon) file for this plugin
	 */
	public String getImageFile();
	
	/**
	 * @return The category of this plugin or null if this is not applicable
	 */
	public String getCategory();
	
	/**
	 * @return True if a separate class loader is needed every time this class is instantiated 
	 */
	public boolean isSeparateClassLoaderNeeded();
	
	/**
	 * @return true if this is considered to be a standard native plugin.
	 */
	public boolean isNativePlugin();
	
	/**
	 * @return All the possible class names that can be loaded with this plugin, split up by type.
	 */
	public Map<Class<?>, String> getClassMap();

	/**
	 * @param id the plugin id to match
	 * @return true if one of the ids matches the given argument. Return false if it doesn't.
	 */
	public boolean matches(String id);
	
	/**
	 * @return An optional location to a help file that the plugin can refer to in case there is a loading problem.  This usually happens if a jar file is not installed correctly (class not found exceptions) etc. 
	 */
	public String getErrorHelpFile();
}
