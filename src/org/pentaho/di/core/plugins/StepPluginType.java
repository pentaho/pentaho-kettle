/**
 * 
 */
package org.pentaho.di.core.plugins;

import java.io.InputStream;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.vfs.FileObject;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.annotations.Step;
import org.pentaho.di.core.exception.KettlePluginException;
import org.pentaho.di.core.exception.KettleXMLException;
import org.pentaho.di.core.vfs.KettleVFS;
import org.pentaho.di.core.xml.XMLHandler;
import org.pentaho.di.trans.step.StepInterface;
import org.pentaho.di.trans.step.StepMetaInterface;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

/**
 * This class represents the step plugin type.
 * 
 * @author matt
 *
 */
@PluginTypeCategoriesOrder(getNaturalCategoriesOrder={"BaseStep.Category.Input",
    "BaseStep.Category.Output"
   ,"BaseStep.Category.Transform"
   ,"BaseStep.Category.Utility"
   ,"BaseStep.Category.Flow"
   ,"BaseStep.Category.Scripting"
   ,"BaseStep.Category.Lookup"
   ,"BaseStep.Category.Joins"
   ,"BaseStep.Category.DataWarehouse"
   ,"BaseStep.Category.Validation"
   ,"BaseStep.Category.Statistics"
   ,"BaseStep.Category.Job"
   ,"BaseStep.Category.Mapping"
   ,"BaseStep.Category.Inline"
   ,"BaseStep.Category.Experimental"
   ,"BaseStep.Category.Deprecated"
   ,"BaseStep.Category.Bulk"},
   i18nPackageClass = StepInterface.class)
@PluginMainClassType(StepMetaInterface.class)
public class StepPluginType extends BasePluginType implements PluginTypeInterface {
	
	private static StepPluginType stepPluginType;
	
	private StepPluginType() {
		super("STEP", "Step");
		populateFolders("steps");
	}
	
	public static StepPluginType getInstance() {
		if (stepPluginType==null) {
			stepPluginType=new StepPluginType();
		}
		return stepPluginType;
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
		String kettleStepsXmlFile = Const.XML_FILE_KETTLE_STEPS;
		
		// Load the plugins for this file...
		//
		try {
			InputStream inputStream = getClass().getResourceAsStream(kettleStepsXmlFile);
			if (inputStream==null) {
				inputStream =  getClass().getResourceAsStream("/"+kettleStepsXmlFile);
			}
			if (inputStream==null) {
				throw new KettlePluginException("Unable to find native step definition file: "+Const.XML_FILE_KETTLE_STEPS);
			}
			Document document = XMLHandler.loadXMLFile(inputStream, null, true, false);
			
			// Document document = XMLHandler.loadXMLFile(kettleStepsXmlFile);
			
			Node stepsNode = XMLHandler.getSubNode(document, "steps");
			List<Node> stepNodes = XMLHandler.getNodes(stepsNode, "step");
			for (Node stepNode : stepNodes) {
				registerPluginFromXmlResource(stepNode, null, this.getClass(), true, null);
			}
			
		} catch (KettleXMLException e) {
			throw new KettlePluginException("Unable to read the kettle steps XML config file: "+kettleStepsXmlFile, e);
		}
	}

	/**
	 * Scan & register internal step plugins
	 */
	protected void registerAnnotations() throws KettlePluginException {
		// We no longer scan for annotations in the class path itself, only in jar files in the plugins/ folders.
	}

	private void handleStepAnnotation(Class<?> clazz, Step step, List<String> libraries, boolean nativeStep) throws KettlePluginException {
		
		String[] ids = step.name(); 
		
		if (ids.length == 1 && Const.isEmpty(ids[0])) { 
			throw new KettlePluginException("No ID specified for plugin with class: "+clazz.getName());
		}
		
		// The package name to get the descriptions or tool tip from...
		//
		String packageName = step.i18nPackageName();
		if (Const.isEmpty(packageName)) packageName = StepInterface.class.getPackage().getName();
		
		// An alternative package to get the description or tool tip from...
		//
		String altPackageName = clazz.getPackage().getName();
		
		// Determine the i18n descriptions of the step description (name), tool tip and category
		//
		String name = getTranslation(step.description(), packageName, altPackageName, clazz);
		String description = getTranslation(step.tooltip(), packageName, altPackageName, clazz);
		String category = getTranslation(step.categoryDescription(), packageName, altPackageName, clazz);
		
		// Register this step plugin...
		//
		

	    Map<Class<?>, String> classMap = new HashMap<Class<?>, String>();
	    classMap.put(StepMetaInterface.class, clazz.getName());
	    
	    // Handle addition Plugin classes to be managed by the registry.
	    PluginClassTypes classTypesAnnotation = clazz.getAnnotation(PluginClassTypes.class);
	    if(classTypesAnnotation != null){
	      for(int i=0; i< classTypesAnnotation.classTypes().length; i++){
	        Class<?> classType = classTypesAnnotation.classTypes()[i];
	        Class<?> implementationType = (classTypesAnnotation.implementationClass().length > i) ? classTypesAnnotation.implementationClass()[i] : null;
	        String className = implementationType.getName();
	        classMap.put(classType, className);
	      }
	    }
			
		PluginInterface stepPlugin = new Plugin(ids, this.getClass(), StepMetaInterface.class, category, name, description, step.image(), step.isSeparateClassLoaderNeeded(), nativeStep, classMap, libraries, null, null);
		registry.registerPlugin(this.getClass(), stepPlugin);
	}

	/**
	 * Scan jar files in a set of plugin folders.  Open these jar files and scan for annotations if they are labeled for annotation scanning.
	 * 
	 * @throws KettlePluginException in case something goes horribly wrong
	 */
	protected void registerPluginJars() throws KettlePluginException {
		
		List<JarFileAnnotationPlugin> jarFilePlugins = findAnnotatedClassFiles(Step.class.getName());
		for (JarFileAnnotationPlugin jarFilePlugin : jarFilePlugins) {
			
			URLClassLoader urlClassLoader = createUrlClassLoader(jarFilePlugin.getJarFile(), getClass().getClassLoader());
			try {
				Class<?> clazz = urlClassLoader.loadClass(jarFilePlugin.getClassFile().getName());
				Step step = clazz.getAnnotation(Step.class);
				List<String> libraries = new ArrayList<String>();
				libraries.add(jarFilePlugin.getJarFile().getFile());
				handleStepAnnotation(clazz, step, libraries, false);
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
						if (pluginNode!=null) {
							registerPluginFromXmlResource(pluginNode, KettleVFS.getFilename(file.getParent()), this.getClass(), false, file.getParent().getURL());
						}
					} catch(Exception e) {
						// We want to report this plugin.xml error, perhaps an XML typo or something like that...
						//
						log.logError("Error found while reading step plugin.xml file: "+file.getName().toString(), e);
					}
				}
			}
		}
	}
	
}
