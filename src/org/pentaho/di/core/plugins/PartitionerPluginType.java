/**
 * 
 */
package org.pentaho.di.core.plugins;

import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.util.List;

import org.apache.commons.vfs.FileObject;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.annotations.PartitionerPlugin;
import org.pentaho.di.core.exception.KettlePluginException;
import org.pentaho.di.core.exception.KettleXMLException;
import org.pentaho.di.core.vfs.KettleVFS;
import org.pentaho.di.core.xml.XMLHandler;
import org.pentaho.di.trans.Partitioner;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

/**
 * This is the partitioner plugin type.
 * 
 * @author matt
 *
 */
@PluginMainClassType(Partitioner.class)
@PluginAnnotationType(PartitionerPlugin.class)
public class PartitionerPluginType extends BasePluginType implements PluginTypeInterface {
	
	private static PartitionerPluginType pluginType;
	
	private PartitionerPluginType() {
		super(PartitionerPlugin.class, "PARTITIONER", "Partitioner");
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
	

  @Override
  protected String extractCategory(Annotation annotation) {
    return "";
  }

  @Override
  protected String extractDesc(Annotation annotation) {
    return ((PartitionerPlugin) annotation).description();
  }

  @Override
  protected String extractID(Annotation annotation) {
    return ((PartitionerPlugin) annotation).id();
  }

  @Override
  protected String extractName(Annotation annotation) {
    return ((PartitionerPlugin) annotation).name();
  }

  @Override
  protected String extractImageFile(Annotation annotation) {
    return null;
  }

  @Override
  protected boolean extractSeparateClassLoader(Annotation annotation) {
    return false;
  }
  
  @Override
  protected String extractI18nPackageName(Annotation annotation) {
    return ((PartitionerPlugin) annotation).i18nPackageName();
  }

}
