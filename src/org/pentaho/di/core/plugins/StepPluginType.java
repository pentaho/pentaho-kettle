/**
 * 
 */
package org.pentaho.di.core.plugins;

import java.io.FileInputStream;
import java.io.InputStream;
import java.lang.annotation.Annotation;
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
   ,"BaseStep.Category.Palo"
   ,"BaseStep.Category.Job"
   ,"BaseStep.Category.Mapping"
   ,"BaseStep.Category.Bulk"
   ,"BaseStep.Category.Inline"
   ,"BaseStep.Category.Experimental"
   ,"BaseStep.Category.Deprecated"
   },
   i18nPackageClass = StepInterface.class)
@PluginMainClassType(StepMetaInterface.class)
@PluginAnnotationType(Step.class)
public class StepPluginType extends BasePluginType implements PluginTypeInterface {
	
	private static StepPluginType stepPluginType;
	
	private StepPluginType() {
		super(Step.class, "STEP", "Step");
		populateFolders("steps");
	}
	
	public static StepPluginType getInstance() {
		if (stepPluginType==null) {
			stepPluginType=new StepPluginType();
		}
		return stepPluginType;
	}
	
	/**
	 * Scan & register internal step plugins
	 */
	protected void registerNatives() throws KettlePluginException {
		// Scan the native steps...
		//
		String kettleStepsXmlFile = Const.XML_FILE_KETTLE_STEPS;
		String alternative = System.getProperty(Const.KETTLE_CORE_STEPS_FILE, null);
		if (!Const.isEmpty(alternative)) {
		  kettleStepsXmlFile = alternative;
		}
		
		// Load the plugins for this file...
		//
		try {
			InputStream inputStream = getClass().getResourceAsStream(kettleStepsXmlFile);
			if (inputStream==null) {
			  inputStream =  getClass().getResourceAsStream("/"+kettleStepsXmlFile);
			}
			// Retry to load a regular file...
			if (inputStream==null && !Const.isEmpty(alternative)) {
			  try {
			    inputStream = new FileInputStream(kettleStepsXmlFile);
			  } catch(Exception e) {
			    throw new KettlePluginException("Unable to load native step plugins '"+kettleStepsXmlFile+"'", e);
			  }
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

  @Override
  protected String extractCategory(Annotation annotation) {
    return ((Step) annotation).categoryDescription();
  }

  @Override
  protected String extractDesc(Annotation annotation) {
    return ((Step) annotation).description();
  }

  @Override
  protected String extractID(Annotation annotation) {
    return ((Step) annotation).id();
  }

  @Override
  protected String extractName(Annotation annotation) {
    return ((Step) annotation).name();
  }
  
  @Override
  protected String extractImageFile(Annotation annotation) {
    return ((Step) annotation).image();
  }
  
  @Override
  protected boolean extractSeparateClassLoader(Annotation annotation) {
    return ((Step) annotation).isSeparateClassLoaderNeeded();
  }

  @Override
  protected String extractI18nPackageName(Annotation annotation) {
    return ((Step) annotation).i18nPackageName();
  }

  @Override
  protected void addExtraClasses(Map<Class<?>, String> classMap, Class<?> clazz, Annotation annotation) {	  
  }
}
