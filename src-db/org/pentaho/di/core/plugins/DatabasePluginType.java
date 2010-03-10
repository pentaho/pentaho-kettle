/**
 * 
 */
package org.pentaho.di.core.plugins;

import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.pentaho.di.core.Const;
import org.pentaho.di.core.exception.KettlePluginException;
import org.pentaho.di.core.logging.LogChannel;

/**
 * This class represents the step plugin type.
 * 
 * @author matt
 *
 */
public class DatabasePluginType extends BasePluginType implements PluginTypeInterface {
	private static DatabasePluginType pluginType;
	
	private DatabasePluginType() {
		super("DATABASE", "Database");
		populateFolders("databases");
	}
	
	public static DatabasePluginType getInstance() {
		if (pluginType==null) {
			pluginType=new DatabasePluginType();
		}
		return pluginType;
	}
	
	/**
	 * Let's put in code here to search for the step plugins..
	 */
	public void searchPlugins() throws KettlePluginException {
		registerAnnotations();
		registerPluginJars();
		registerXmlPlugins();
	}

	/**
	 * Scan & register internal step plugins
	 */
	protected void registerNatives() throws KettlePluginException {
	}

	/**
	 * Scan & register internal step plugins
	 */
	protected void registerAnnotations() throws KettlePluginException {

		List<Class<?>> classes = getAnnotatedClasses(DatabaseMetaPlugin.class);
		LogChannel.GENERAL.logDetailed("Found "+classes.size()+" classes annotated with @DatabaseMetaPlugin");
		for (Class<?> clazz : classes)
		{
			DatabaseMetaPlugin databaseMetaPlugin = clazz.getAnnotation(DatabaseMetaPlugin.class);
			handleDatabaseMetaPluginAnnotation(clazz, databaseMetaPlugin, new ArrayList<String>(), true, null);
		}		
	}
	
	private void handleDatabaseMetaPluginAnnotation(Class<?> clazz, DatabaseMetaPlugin databaseMetaPlugin, List<String> libraries, boolean nativeStep, URL pluginFolder) throws KettlePluginException {
		
		String[] ids = new String[] { databaseMetaPlugin.type(), }; 
		
		if (ids.length == 1 && Const.isEmpty(ids[0])) { 
			throw new KettlePluginException("No ID specified for plugin with class: "+clazz.getName());
		}
		
		String name = databaseMetaPlugin.typeDescription();
		String description = null;
		String category = null;
		
		// Register this step plugin...
		//
		Map<Class<?>, String> classMap = new HashMap<Class<?>, String>();
		classMap.put(clazz.getClass(), clazz.getName());
		
		PluginInterface plugin = new Plugin(ids, this.getClass(), clazz.getClass(), category, name, description, null, false, nativeStep, classMap, libraries, "", pluginFolder);
		registry.registerPlugin(this.getClass(), plugin);
	}

	/**
	 * Scan jar files in a set of plugin folders.  Open these jar files and scan for annotations if they are labeled for annotation scanning.
	 * 
	 * @throws KettlePluginException in case something goes horribly wrong
	 */
	protected void registerPluginJars() throws KettlePluginException {
		
		List<JarFileAnnotationPlugin> jarFilePlugins = findAnnotatedClassFiles(DatabaseMetaPlugin.class.getName());
		for (JarFileAnnotationPlugin jarFilePlugin : jarFilePlugins) {
			
			URLClassLoader urlClassLoader = new KettleURLClassLoader(new URL[] { jarFilePlugin.getJarFile(), }, getClass().getClassLoader());
			try {
				Class<?> clazz = urlClassLoader.loadClass(jarFilePlugin.getClassFile().getName());
				DatabaseMetaPlugin databaseMetaPlugin = clazz.getAnnotation(DatabaseMetaPlugin.class);
				List<String> libraries = new ArrayList<String>();
				libraries.add(jarFilePlugin.getJarFile().getFile());
				handleDatabaseMetaPluginAnnotation(clazz, databaseMetaPlugin, libraries, false, jarFilePlugin.getPluginFolder());
			} catch(ClassNotFoundException e) {
				// Ignore for now, don't know if it's even possible.
			}
		}
	}
	
	protected void registerXmlPlugins() throws KettlePluginException {
	}
	
	public String[] getNaturalCategoriesOrder() {
		return new String[0];
	}
}
