/**
 * 
 */
package org.pentaho.di.core.plugins;

import java.io.InputStream;
import java.lang.annotation.Annotation;
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
    ,"JobCategory.Category.Utility"
    ,"JobCategory.Category.Repository"
    ,"JobCategory.Category.FileTransfer"
    ,"JobCategory.Category.Experimental"
},
   i18nPackageClass = JobMeta.class)
@PluginMainClassType(JobEntryInterface.class)
@PluginAnnotationType(JobEntry.class)
public class JobEntryPluginType extends BasePluginType implements PluginTypeInterface {
	private static Class<?> PKG = JobMeta.class; // for i18n purposes, needed by Translator2!!   $NON-NLS-1$

  public static final String GENERAL_CATEGORY = BaseMessages.getString(PKG, "JobCategory.Category.General");
	
	private static JobEntryPluginType pluginType;
	
	private JobEntryPluginType() {
		super(JobEntry.class, "JOBENTRY", "Job entry");
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
    String alternative = System.getProperty(Const.KETTLE_CORE_JOBENTRIES_FILE, null);
    if (!Const.isEmpty(alternative)) {
      kettleJobEntriesXmlFile = alternative;
    }

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

  @Override
  protected String extractCategory(Annotation annotation) {
    return ((JobEntry) annotation).categoryDescription();
  }

  @Override
  protected String extractDesc(Annotation annotation) {
    return ((JobEntry) annotation).description();
  }

  @Override
  protected String extractID(Annotation annotation) {
    return ((JobEntry) annotation).id();
  }

  @Override
  protected String extractName(Annotation annotation) {
    return ((JobEntry) annotation).name();
  }
  
  @Override
  protected String extractImageFile(Annotation annotation) {
    return ((JobEntry) annotation).image();
  }

  @Override
  protected boolean extractSeparateClassLoader(Annotation annotation) {
    return false;
  }
  
  @Override
  protected String extractI18nPackageName(Annotation annotation) {
    return ((JobEntry) annotation).i18nPackageName();
  }

  @Override
  protected void addExtraClasses(Map<Class<?>, String> classMap, Class<?> clazz, Annotation annotation) {	  
  }
}
