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
import org.pentaho.di.core.annotations.PartitionerPlugin;
import org.pentaho.di.core.exception.KettlePluginException;
import org.pentaho.di.core.exception.KettleXMLException;
import org.pentaho.di.core.vfs.KettleVFS;
import org.pentaho.di.core.xml.XMLHandler;
import org.pentaho.di.trans.Partitioner;
import org.pentaho.di.trans.step.StepInterface;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

/**
 * This is the partitioner plugin type.
 * 
 * @author matt
 *
 */
@PluginMainClassType(Partitioner.class)
public class PartitionerPluginType extends BasePluginType implements PluginTypeInterface {
	
	private static PartitionerPluginType pluginType;
	
	private PartitionerPluginType() {
		super("PARTITIONER", "Partitioner");
		populateFolders("steps");
	}
	
	public static PartitionerPluginType getInstance() {
		if (pluginType==null) {
			pluginType=new PartitionerPluginType();
		}
		return pluginType;
	}
	
	/**
	 * Scan & register internal step plugins
	 */
	protected void registerNatives() throws KettlePluginException {
		// Scan the native steps...
		//
		String kettlePartitionerXmlFile = Const.XML_FILE_KETTLE_PARTITION_PLUGINS;
		
		// Load the plugins for this file...
		//
		try {
			InputStream inputStream = getClass().getResourceAsStream(kettlePartitionerXmlFile);
			if (inputStream==null) {
				inputStream = getClass().getResourceAsStream("/"+kettlePartitionerXmlFile);
			}
			if (inputStream==null) {
				throw new KettlePluginException("Unable to find native partition plugins definition file: "+Const.XML_FILE_KETTLE_PARTITION_PLUGINS);
			}
			Document document = XMLHandler.loadXMLFile(inputStream, null, true, false);
			
			// Document document = XMLHandler.loadXMLFile(kettleStepsXmlFile);
			
			Node stepsNode = XMLHandler.getSubNode(document, "plugins");
			List<Node> stepNodes = XMLHandler.getNodes(stepsNode, "plugin-partitioner");
			for (Node stepNode : stepNodes) {
				registerPluginFromXmlResource(stepNode, null, this.getClass(), true, null);
			}
			
		} catch (KettleXMLException e) {
			throw new KettlePluginException("Unable to read the kettle steps XML config file: "+kettlePartitionerXmlFile, e);
		}
	}

	/**
	 * Scan & register internal step plugins
	 */
	protected void registerAnnotations() throws KettlePluginException {
		// This is no longer done because it was deemed too slow.  Only jar files in the plugins/ folders are scanned for annotations.
	}

	private void handlePartitionerAnnotation(Class<?> clazz, PartitionerPlugin partitioner, List<String> libraries, boolean nativeStep) throws KettlePluginException {
		
		// saving old value for change the loglevel to BASIC to avoid i18n messages, see below
		//
		String[] ids = partitioner.name(); 
		
		if (ids.length == 1 && ids[0].equals("")) { 
			throw new KettlePluginException("No ID specified for plugin with class: "+clazz.getName());
		}
		
		// The package name to get the descriptions or tool tip from...
		//
		String packageName = partitioner.i18nPackageName();
		if (Const.isEmpty(packageName)) packageName = StepInterface.class.getPackage().getName();
		
		// An alternative package to get the description or tool tip from...
		//
		String altPackageName = clazz.getPackage().getName();
		
		// Determine the i18n descriptions of the step description (name), tool tip and category
		//
		String description = getTranslation(partitioner.description(), packageName, altPackageName, clazz);
		String tooltip = getTranslation(partitioner.tooltip(), packageName, altPackageName, clazz);
		String category = null;
		
		// Register this step plugin...
		//

		Map<Class<?>, String> classMap = new HashMap<Class<?>, String>();
		classMap.put(Partitioner.class, clazz.getName());
		
		PluginInterface stepPlugin = new Plugin(ids, this.getClass(), Partitioner.class, category, description, tooltip, null, false, nativeStep, classMap, libraries, null, null);
		registry.registerPlugin(this.getClass(), stepPlugin);
	}

	/**
	 * Scan jar files in a set of plugin folders.  Open these jar files and scan for annotations if they are labeled for annotation scanning.
	 * 
	 * @throws KettlePluginException in case something goes horribly wrong
	 */
	protected void registerPluginJars() throws KettlePluginException {
		
		List<JarFileAnnotationPlugin> jarFilePlugins = findAnnotatedClassFiles(PartitionerPlugin.class.getName());
		for (JarFileAnnotationPlugin jarFilePlugin : jarFilePlugins) {
			
			URLClassLoader urlClassLoader = new URLClassLoader(new URL[] { jarFilePlugin.getJarFile(), });
			try {
				Class<?> clazz = urlClassLoader.loadClass(jarFilePlugin.getClassFile().getName());
				PartitionerPlugin partitioner = clazz.getAnnotation(PartitionerPlugin.class);
				List<String> libraries = new ArrayList<String>();
				libraries.add(jarFilePlugin.getJarFile().getFile());
				handlePartitionerAnnotation(clazz, partitioner, libraries, false);
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
						Node pluginNode = XMLHandler.getSubNode(document, "partitioner-plugin");
						if (pluginNode!=null) {
							registerPluginFromXmlResource(pluginNode, KettleVFS.getFilename(file.getParent()), this.getClass(), false, file.getParent().getURL());
						}
					} catch(Exception e) {
						// We want to report this plugin.xml error, perhaps an XML typo or something like that...
						//
						log.logError("Error found while reading partitioning plugin.xml file: "+file.getName().toString(), e);
					}
				}
			}
		}
	}
	
}
