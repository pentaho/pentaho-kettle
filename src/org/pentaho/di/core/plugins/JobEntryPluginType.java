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
import org.pentaho.di.core.annotations.JobEntry;
import org.pentaho.di.core.exception.KettlePluginException;
import org.pentaho.di.core.exception.KettleXMLException;
import org.pentaho.di.core.vfs.KettleVFS;
import org.pentaho.di.core.xml.XMLHandler;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.job.JobMeta;
import org.pentaho.di.job.entry.JobEntryInterface;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

/**
 * This plugin type handles the job entries.
 * 
 * @author matt
 *
 */

@PluginTypeCategoriesOrder(getNaturalCategoriesOrder={"BaseStep.Category.Input",
    "JobCategory.Category.General"
    ,"JobCategory.Category.Mail"
    ,"JobCategory.Category.FileManagement"
    ,"JobCategory.Category.Conditions"
    ,"JobCategory.Category.Scripting"
    ,"JobCategory.Category.BulkLoading"
    ,"JobCategory.Category.XML"
    ,"JobCategory.Category.Repository"
    ,"JobCategory.Category.FileTransfer"
    ,"JobCategory.Category.Experimental"},
   i18nPackageClass = JobMeta.class)
 @PluginMainClassType(JobEntryInterface.class)
public class JobEntryPluginType extends BasePluginType implements PluginTypeInterface {
	private static Class<?> PKG = JobMeta.class; // for i18n purposes, needed by Translator2!!   $NON-NLS-1$

  public static final String GENERAL_CATEGORY = BaseMessages.getString(PKG, "JobCategory.Category.General");
	
	private static JobEntryPluginType pluginType;
	
	private JobEntryPluginType() {
		super("JOBENTRY", "Job entry");
		populateFolders("jobentries");
	}
	
	public static JobEntryPluginType getInstance() {
		if (pluginType==null) {
			pluginType=new JobEntryPluginType();
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
		String kettleJobEntriesXmlFile = Const.XML_FILE_KETTLE_JOB_ENTRIES;
		
		// Load the plugins for this file...
		//
		try {
			InputStream inputStream = getClass().getResourceAsStream(kettleJobEntriesXmlFile);
			if (inputStream==null) {
				inputStream =  getClass().getResourceAsStream("/"+kettleJobEntriesXmlFile);
			}
			if (inputStream==null) {
				throw new KettlePluginException("Unable to find native step definition file: "+Const.XML_FILE_KETTLE_JOB_ENTRIES);
			}
			Document document = XMLHandler.loadXMLFile(inputStream, null, true, false);
			
			// Document document = XMLHandler.loadXMLFile(kettleStepsXmlFile);
			
			Node entriesNode = XMLHandler.getSubNode(document, "job-entries");
			List<Node> entryNodes = XMLHandler.getNodes(entriesNode, "job-entry");
			for (Node entryNode : entryNodes) {
				registerPluginFromXmlResource(entryNode, null, this.getClass(), true, null);
			}
			
		} catch (KettleXMLException e) {
			throw new KettlePluginException("Unable to read the kettle job entries XML config file: "+kettleJobEntriesXmlFile, e);
		}
	}

	/**
	 * Scan & register internal job entry plugins
	 */
	protected void registerAnnotations() throws KettlePluginException {
		// This is no longer done because it was deemed too slow.  Only jar files in the plugins/ folders are scanned for annotations.
	}

	private void handleJobEntryAnnotation(Class<?> clazz, JobEntry jobEntry, List<String> libraries, boolean nativeJobEntry) throws KettlePluginException {
		
		// Only one ID for now
		String[] ids = new String[] { jobEntry.id(), }; 
		
		if (ids.length == 1 && Const.isEmpty(ids[0])) { 
			throw new KettlePluginException("No ID specified for plugin with class: "+clazz.getName());
		}
		
		// The package name to get the descriptions or tool tip from...
		//
		String packageName = jobEntry.i18nPackageName();
		if (Const.isEmpty(packageName)) packageName = JobEntryInterface.class.getPackage().getName();
		
		// An alternative package to get the description or tool tip from...
		//
		String altPackageName = clazz.getPackage().getName();
		
		// Determine the i18n descriptions of the step description (name), tool tip and category
		//
		String name = getTranslation(jobEntry.name(), packageName, altPackageName, clazz);
		String description = getTranslation(jobEntry.description(), packageName, altPackageName, clazz);
		String category = getTranslation(jobEntry.categoryDescription(), packageName, altPackageName, clazz);
		
		// Register this step plugin...
		//

	    Map<Class<?>, String> classMap = new HashMap<Class<?>, String>();
	    
	    classMap.put(JobEntryInterface.class, clazz.getName());
	    
	    PluginClassTypes classTypesAnnotation = clazz.getAnnotation(PluginClassTypes.class);
	    if(classTypesAnnotation != null){
	      for(int i=0; i< classTypesAnnotation.classTypes().length; i++){
	        Class<?> classType = classTypesAnnotation.classTypes()[i];
	        Class<?> implementationType = (classTypesAnnotation.implementationClass().length > i) ? classTypesAnnotation.implementationClass()[i] : null;
	        String className = implementationType.getName();
	        classMap.put(classType, className);
	      }
	    }
		
		PluginInterface stepPlugin = new Plugin(ids, this.getClass(), JobEntryInterface.class, category, name, description, jobEntry.image(), false, nativeJobEntry, classMap, libraries, null, null);
		registry.registerPlugin(this.getClass(), stepPlugin);
	}

	/**
	 * Scan jar files in a set of plugin folders.  Open these jar files and scan for annotations if they are labeled for annotation scanning.
	 * 
	 * @throws KettlePluginException in case something goes horribly wrong
	 */
	protected void registerPluginJars() throws KettlePluginException {
		
		List<JarFileAnnotationPlugin> jarFilePlugins = findAnnotatedClassFiles(JobEntry.class.getName());
		for (JarFileAnnotationPlugin jarFilePlugin : jarFilePlugins) {
			
			URLClassLoader urlClassLoader = new KettleURLClassLoader(new URL[] { jarFilePlugin.getJarFile(), }, getClass().getClassLoader());
			try {
				Class<?> clazz = urlClassLoader.loadClass(jarFilePlugin.getClassFile().getName());
				JobEntry jobEntry = clazz.getAnnotation(JobEntry.class);
				List<String> libraries = new ArrayList<String>();
				libraries.add(jarFilePlugin.getJarFile().getFile());
				handleJobEntryAnnotation(clazz, jobEntry, libraries, false);
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

						registerPluginFromXmlResource(pluginNode, KettleVFS.getFilename(file.getParent()), this.getClass(), false, file.getParent().getURL());
					} catch(Exception e) {
						// We want to report this plugin.xml error, perhaps an XML typo or something like that...
						//
						log.logError("Error found while reading job entry plugin.xml file: "+file.getName().toString(), e);
					}
				}
			}
		}
	}
}
