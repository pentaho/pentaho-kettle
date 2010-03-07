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

import org.apache.commons.vfs.FileObject;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.annotations.RepositoryPlugin;
import org.pentaho.di.core.exception.KettlePluginException;
import org.pentaho.di.core.exception.KettleXMLException;
import org.pentaho.di.core.vfs.KettleVFS;
import org.pentaho.di.core.xml.XMLHandler;
import org.pentaho.di.repository.Repository;
import org.pentaho.di.repository.RepositoryMeta;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

/**
 * This class represents the repository plugin type.
 * 
 * @author matt
 *
 */
@PluginMainClassType(Repository.class)
@PluginClassTypes(classTypes = { RepositoryMeta.class }, xmlNodeNames = { "meta-classname" })
public class RepositoryPluginType extends BasePluginType implements PluginTypeInterface {

	private static RepositoryPluginType pluginType;
	
	private RepositoryPluginType() {
		super("REPOSITORY_TYPE", "Repository type");
		populateFolders("repositories");
	}
	
	public static RepositoryPluginType getInstance() {
		if (pluginType==null) {
			pluginType=new RepositoryPluginType();
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
		// Scan the native steps...
		//
		String xmlFile = Const.XML_FILE_KETTLE_REPOSITORIES;
		
		// Load the plugins for this file...
		//
		try {
			InputStream inputStream = getClass().getResourceAsStream(xmlFile);
			if (inputStream==null) {
				inputStream =  getClass().getResourceAsStream("/"+xmlFile);
			}
			if (inputStream==null) {
				throw new KettlePluginException("Unable to find native repository type definition file: "+xmlFile);
			}
			Document document = XMLHandler.loadXMLFile(inputStream, null, true, false);
			
			// Document document = XMLHandler.loadXMLFile(kettleStepsXmlFile);
			
			Node repsNode = XMLHandler.getSubNode(document, "repositories");
			List<Node> repsNodes = XMLHandler.getNodes(repsNode, "repository");
			for (Node repNode : repsNodes) {
				registerPluginFromXmlResource(repNode, null, this.getClass(), true);
			}			
		} catch (KettleXMLException e) {
			throw new KettlePluginException("Unable to read the kettle repositories XML config file: "+xmlFile, e);
		}
	}

	/**
	 * Scan & register internal repository type plugins
	 */
	protected void registerAnnotations() throws KettlePluginException {

		List<Class<?>> classes = getAnnotatedClasses(RepositoryPlugin.class);
		for (Class<?> clazz : classes)
		{
			RepositoryPlugin repositoryPlugin = clazz.getAnnotation(RepositoryPlugin.class);
			handleRepositoryPluginAnnotation(clazz, repositoryPlugin, new ArrayList<String>(), true);
		}		
	}

	private void handleRepositoryPluginAnnotation(Class<?> clazz, RepositoryPlugin repositoryPlugin, List<String> libraries, boolean nativeRepositoryType) throws KettlePluginException {
		
		// Only one ID for now
		String[] ids = new String[] { repositoryPlugin.id(), }; 
		
		if (ids.length == 1 && Const.isEmpty(ids[0])) { 
			throw new KettlePluginException("No ID specified for plugin with class: "+clazz.getName());
		}
		
		// The package name to get the descriptions or tool tip from...
		//
		String packageName = repositoryPlugin.i18nPackageName();
		if (Const.isEmpty(packageName)) packageName = Repository.class.getPackage().getName();
		
		// An alternative package to get the description or tool tip from...
		//
		String altPackageName = clazz.getPackage().getName();
		
		// Determine the i18n descriptions of the step description (name), tool tip and category
		//
		String name = getTranslation(repositoryPlugin.name(), packageName, altPackageName, clazz);
		String description = getTranslation(repositoryPlugin.description(), packageName, altPackageName, clazz);
		String category = null; // No categories yet.
		
		// Register this step plugin...
		//
		//Map<PluginClassType, String> classMap = new HashMap<PluginClassType, String>();
		
	    Map<Class<?>, String> classMap = new HashMap<Class<?>, String>();
	    classMap.put(Repository.class, clazz.getName());
	    classMap.put(RepositoryMeta.class, repositoryPlugin.metaClass());
	    
	    PluginClassTypes extraTypes = clazz.getAnnotation(PluginClassTypes.class);
	    if(extraTypes != null){
	      for(int i=0; i< extraTypes.classTypes().length; i++){
	        classMap.put(extraTypes.classTypes()[i], extraTypes.implementationClass()[i].getName());
	      }
	    }
		
		PluginInterface stepPlugin = new Plugin(ids, this.getClass(), Repository.class, category, name, description, null, false, nativeRepositoryType, classMap, libraries, null);
		registry.registerPlugin(this.getClass(), stepPlugin);
	}

	/**
	 * Scan jar files in a set of plugin folders.  Open these jar files and scan for annotations if they are labeled for annotation scanning.
	 * 
	 * @throws KettlePluginException in case something goes horribly wrong
	 */
	protected void registerPluginJars() throws KettlePluginException {
		
		List<JarFileAnnotationPlugin> jarFilePlugins = findAnnotatedClassFiles(RepositoryPlugin.class.getName());
		for (JarFileAnnotationPlugin jarFilePlugin : jarFilePlugins) {
			
			URLClassLoader urlClassLoader = new KettleURLClassLoader(new URL[] { jarFilePlugin.getJarFile(), }, getClass().getClassLoader());
			try {
				Class<?> clazz = urlClassLoader.loadClass(jarFilePlugin.getClassFile().getName());
				RepositoryPlugin repositoryPlugin = clazz.getAnnotation(RepositoryPlugin.class);
				List<String> libraries = new ArrayList<String>();
				libraries.add(jarFilePlugin.getJarFile().getFile());
				handleRepositoryPluginAnnotation(clazz, repositoryPlugin, libraries, false);
			} catch(ClassNotFoundException e) {
				// Ignore for now, don't know if it's even possible.
			}
		}
	}
	
	protected void registerXmlPlugins() throws KettlePluginException {
		for (PluginFolderInterface folder : pluginFolders) {
			
			if (folder.isPluginXmlFolder()) {
				List<FileObject> pluginXmlFiles = findPluginXmlFiles(folder.getFolder());
				for (FileObject file : pluginXmlFiles) {
					
					try {
						Document document = XMLHandler.loadXMLFile(file);
						Node pluginNode = XMLHandler.getSubNode(document, "plugin");

						registerPluginFromXmlResource(pluginNode, KettleVFS.getFilename(file.getParent()), this.getClass(), false);
					} catch(Exception e) {
						// We want to report this plugin.xml error, perhaps an XML typo or something like that...
						//
						log.logError("Error found while reading repository plugin.xml file: "+file.getName().toString(), e);
					}
				}
			}
		}
	}
	
}
