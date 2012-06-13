/*******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2012 by Pentaho : http://www.pentaho.com
 *
 *******************************************************************************
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 ******************************************************************************/

package org.pentaho.di.core.plugins;

import java.io.FileInputStream;
import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.util.List;
import java.util.Map;

import org.apache.commons.vfs.FileObject;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.annotations.CarteServlet;
import org.pentaho.di.core.annotations.Step;
import org.pentaho.di.core.exception.KettlePluginException;
import org.pentaho.di.core.exception.KettleXMLException;
import org.pentaho.di.core.vfs.KettleVFS;
import org.pentaho.di.core.xml.XMLHandler;
import org.pentaho.di.trans.step.StepMetaInterface;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

/**
 * This class represents the carte plugin type.
 * 
 * @author matt
 *
 */
@PluginMainClassType(StepMetaInterface.class)
@PluginAnnotationType(CarteServlet.class)
public class CartePluginType extends BasePluginType implements PluginTypeInterface {
	
	private static CartePluginType cartePluginType;
	
	private CartePluginType() {
		super(CarteServlet.class, "CARTE_SERVLET", "Carte Servlet");
		populateFolders("servlets");
	}
	
	public static CartePluginType getInstance() {
		if (cartePluginType==null) {
			cartePluginType=new CartePluginType();
		}
		return cartePluginType;
	}
	
	/**
	 * Scan & register internal step plugins
	 */
	protected void registerNatives() throws KettlePluginException {
		// Scan the native steps...
		//
		String kettleServletsXmlFile = Const.XML_FILE_KETTLE_SERVLETS;
		String alternative = System.getProperty(Const.KETTLE_CORE_SERVLETS_FILE, null);
		if (!Const.isEmpty(alternative)) {
		  kettleServletsXmlFile = alternative;
		}
		
		// Load the plugins for this file...
		//
		try {
			InputStream inputStream = getClass().getResourceAsStream(kettleServletsXmlFile);
			if (inputStream==null) {
			  inputStream =  getClass().getResourceAsStream("/"+kettleServletsXmlFile);
			}
			// Retry to load a regular file...
			if (inputStream==null && !Const.isEmpty(alternative)) {
			  try {
			    inputStream = new FileInputStream(kettleServletsXmlFile);
			  } catch(Exception e) {
			    throw new KettlePluginException("Unable to load native servlet plugins '"+kettleServletsXmlFile+"'", e);
			  }
			}
			if (inputStream==null) {
				throw new KettlePluginException("Unable to find native servlets definition file: '"+kettleServletsXmlFile+"'");
			}
			Document document = XMLHandler.loadXMLFile(inputStream, null, true, false);
			
			// Document document = XMLHandler.loadXMLFile(kettleStepsXmlFile);
			
			Node servletsNode = XMLHandler.getSubNode(document, "servlets");
			List<Node> servletNodes = XMLHandler.getNodes(servletsNode, "servlet");
			for (Node servletNode : servletNodes) {
				registerPluginFromXmlResource(servletNode, null, this.getClass(), true, null);
			}
			
		} catch (KettleXMLException e) {
			throw new KettlePluginException("Unable to read the kettle servlets XML config file: '"+kettleServletsXmlFile+"'", e);
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
