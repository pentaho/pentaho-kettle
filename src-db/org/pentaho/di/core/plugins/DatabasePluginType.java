/**
 * 
 */
package org.pentaho.di.core.plugins;

import java.io.InputStream;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.pentaho.di.core.Const;
import org.pentaho.di.core.database.DatabaseInterface;
import org.pentaho.di.core.exception.KettlePluginException;
import org.pentaho.di.core.exception.KettleXMLException;
import org.pentaho.di.core.xml.XMLHandler;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

/**
 * This class represents the step plugin type.
 * 
 * @author matt
 *
 */
@PluginMainClassType(DatabaseInterface.class)
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
		registerNatives();
		registerAnnotations();
		registerPluginJars();
		registerXmlPlugins();
	}

	/**
	 * Scan & register internal step plugins
	 */
	protected void registerNatives() throws KettlePluginException {
		// Scan the native database types...
		//
		String xmlFile = Const.XML_FILE_KETTLE_DATABASE_TYPES;
		
		// Load the plugins for this file...
		//
		try {
			InputStream inputStream = getClass().getResourceAsStream(xmlFile);
			if (inputStream==null) {
				inputStream =  getClass().getResourceAsStream("/"+xmlFile);
			}
			if (inputStream==null) {
				throw new KettlePluginException("Unable to find native kettle database types definition file: "+xmlFile);
			}
			Document document = XMLHandler.loadXMLFile(inputStream, null, true, false);
			
			Node repsNode = XMLHandler.getSubNode(document, "database-types");
			List<Node> repsNodes = XMLHandler.getNodes(repsNode, "database-type");
			for (Node repNode : repsNodes) {
				registerPluginFromXmlResource(repNode, null, this.getClass(), true, null);
			}			
		} catch (KettleXMLException e) {
			throw new KettlePluginException("Unable to read the kettle database types XML config file: "+xmlFile, e);
		}
	}

	/**
	 * Scan & register internal step plugins
	 */
	protected void registerAnnotations() throws KettlePluginException {
		// This is no longer done because it was deemed too slow.  Only jar files in the plugins/ folders are scanned for annotations.
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
