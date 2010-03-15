/**
 * 
 */
package org.pentaho.di.core.plugins;

import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.util.List;

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
		super(DatabaseMetaPlugin.class, "DATABASE", "Database");
		populateFolders("databases");
	}
	
	public static DatabasePluginType getInstance() {
		if (pluginType==null) {
			pluginType=new DatabasePluginType();
		}
		return pluginType;
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
	
	protected void registerXmlPlugins() throws KettlePluginException {
	}
	
	public String[] getNaturalCategoriesOrder() {
		return new String[0];
	}


  @Override
  protected String extractCategory(Annotation annotation) {
    return "";
  }

  @Override
  protected String extractDesc(Annotation annotation) {
    return ((DatabaseMetaPlugin) annotation).typeDescription();
  }

  @Override
  protected String extractID(Annotation annotation) {
    return ((DatabaseMetaPlugin) annotation).type();
  }

  @Override
  protected String extractName(Annotation annotation) {
    return ((DatabaseMetaPlugin) annotation).type();
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
    return null;
  }
}
