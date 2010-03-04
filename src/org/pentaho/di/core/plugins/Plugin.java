/**
 * 
 */
package org.pentaho.di.core.plugins;

import java.util.List;
import java.util.Map;

import org.pentaho.di.core.Const;

/**
 * This describes the plugin itself, the IDs it listens too, what libraries (jar
 * files) it uses, the names, the i18n details, etc.
 * 
 * @author matt
 * 
 */
public class Plugin implements PluginInterface {

	private String						category;
	private String						name;
	private String						description;
	private String[]					ids;
	private Class<? extends PluginTypeInterface>			pluginType;
	private String						imageFile;
	private boolean						separateClassLoaderNeeded;
	private boolean						nativePlugin;
	private Map<Class, String>	classMap;
	private List<String>				libraries;
	private String                      errorHelpFile;
	private Class             mainType;

	/**
	 * @param ids
	 * @param pluginType
	 * @param category
	 * @param name
	 * @param description
	 * @param imageFile
	 * @param seaerateClassLoaderNeeded
	 * @param nativePlugin
	 * @param classMap
	 * @param libraries
	 */
	public Plugin(String[] ids, Class<? extends PluginTypeInterface> pluginType, Class mainType, String category, String name, String description, String imageFile, boolean seaerateClassLoaderNeeded, boolean nativePlugin, Map<Class, String> classMap, List<String> libraries, String errorHelpFile) {
		this.ids = ids;
		this.pluginType = pluginType;
		this.mainType = mainType;
		this.category = category;
		this.name = name;
		this.description = description;
		this.imageFile = imageFile;
		this.separateClassLoaderNeeded = seaerateClassLoaderNeeded;
		this.nativePlugin = nativePlugin;
		this.classMap = classMap;
		this.libraries = libraries;
		this.errorHelpFile = errorHelpFile;
	}
	
	@Override
	public String toString() {
		return ids[0]+"/"+name+"{"+pluginType+"}";
	}
	
	@Override
	public boolean equals(Object obj) {
		if (obj==null) return false;
		if (!(obj instanceof Plugin)) return false;
		
		Plugin plugin = (Plugin)obj;
		
		// All the IDs have to be the same to match, otherwise it's a different plugin
		// This might be a bit over the top, usually we only have a single ID
		//
		if (ids.length!=plugin.ids.length) return false;
		for (int i=0;i<ids.length;i++) {
			if (!ids[i].equals(plugin.ids[i])) {
				return false;
			}
		}
		
		return true;
	}
	
	@Override
	public int hashCode() {
		return ids[0].hashCode();
	}

	public boolean matches(String id) {
		return Const.indexOfString(id, ids)>=0;
	}

	/**
	 * @return the category
	 */
	public String getCategory() {
		return category;
	}

	/**
	 * @param category
	 *            the category to set
	 */
	public void setCategory(String category) {
		this.category = category;
	}

	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * @param name
	 *            the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * @return the description
	 */
	public String getDescription() {
		return description;
	}

	/**
	 * @param description
	 *            the description to set
	 */
	public void setDescription(String description) {
		this.description = description;
	}

	/**
	 * @return the ids
	 */
	public String[] getIds() {
		return ids;
	}

	/**
	 * @param ids
	 *            the ids to set
	 */
	public void setIds(String[] ids) {
		this.ids = ids;
	}

	/**
	 * @return the pluginType
	 */
	public Class<? extends PluginTypeInterface> getPluginType() {
		return pluginType;
	}

	/**
	 * @param pluginType
	 *            the pluginType to set
	 */
	public void setPluginType(Class<? extends PluginTypeInterface> pluginType) {
		this.pluginType = pluginType;
	}

	/**
	 * @return the imageFile
	 */
	public String getImageFile() {
		return imageFile;
	}

	/**
	 * @param imageFile
	 *            the imageFile to set
	 */
	public void setImageFile(String imageFile) {
		this.imageFile = imageFile;
	}

	/**
	 * @return the separateClassLoaderNeeded
	 */
	public boolean isSeparateClassLoaderNeeded() {
		return separateClassLoaderNeeded;
	}

	/**
	 * @param separateClassLoaderNeeded
	 *            the separateClassLoaderNeeded to set
	 */
	public void setSaperateClassLoaderNeeded(boolean separateClassLoaderNeeded) {
		this.separateClassLoaderNeeded = separateClassLoaderNeeded;
	}

	/**
	 * @return the nativePlugin
	 */
	public boolean isNativePlugin() {
		return nativePlugin;
	}

	/**
	 * @param nativePlugin
	 *            the nativePlugin to set
	 */
	public void setNativePlugin(boolean nativePlugin) {
		this.nativePlugin = nativePlugin;
	}

	/**
	 * @return the classMap
	 */
	public Map<Class, String> getClassMap() {
		return classMap;
	}

	/**
	 * @param classMap
	 *            the classMap to set
	 */
	public void setClassMap(Map<Class, String> classMap) {
		this.classMap = classMap;
	}

	/**
	 * @return the libraries
	 */
	public List<String> getLibraries() {
		return libraries;
	}

	/**
	 * @param libraries the libraries to set
	 */
	public void setLibraries(List<String> libraries) {
		this.libraries = libraries;
	}

	/**
	 * @return the errorHelpFile
	 */
	public String getErrorHelpFile() {
		return errorHelpFile;
	}

	/**
	 * @param errorHelpFile the errorHelpFile to set
	 */
	public void setErrorHelpFile(String errorHelpFile) {
		this.errorHelpFile = errorHelpFile;
	}

  public Class getMainType() {
    return mainType;
  }
	
	
	
}
