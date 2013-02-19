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

import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.util.List;
import java.util.Map;

import org.apache.commons.vfs.FileObject;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.annotations.DataFactoryPlugin;
import org.pentaho.di.core.annotations.RepositoryPlugin;
import org.pentaho.di.core.exception.KettlePluginException;
import org.pentaho.di.core.exception.KettleXMLException;
import org.pentaho.di.core.vfs.KettleVFS;
import org.pentaho.di.core.xml.XMLHandler;
import org.pentaho.di.datafactory.DynamicDatasource;
import org.pentaho.di.repository.RepositoryMeta;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

/**
 * This class represents the datafactory plugin type. 
 * 
 * @author Gretchen Moran
 *
 */
@PluginMainClassType(DynamicDatasource.class)
@PluginAnnotationType(DataFactoryPlugin.class)
public class DataFactoryPluginType extends BasePluginType implements PluginTypeInterface {

	private static DataFactoryPluginType pluginType;
	
	private DataFactoryPluginType() {
		super(DataFactoryPlugin.class, "DATA_FACTORY_TYPE", "Datafactory type");
		this.populateFolders(null);
	}
	
	public static DataFactoryPluginType getInstance() {
		if (pluginType==null) {
			pluginType=new DataFactoryPluginType();
		}
		return pluginType;
	}
	

	/**
	 * Scan & register internal step plugins
	 */
	protected void registerNatives() throws KettlePluginException {
	}


	
	protected void registerXmlPlugins() throws KettlePluginException {
	}

  @Override
  protected String extractCategory(Annotation annotation) {
    return "";
  }

  @Override
  protected String extractDesc(Annotation annotation) {
    return ((DataFactoryPlugin) annotation).description();
  }

  @Override
  protected String extractID(Annotation annotation) {
    return ((DataFactoryPlugin) annotation).id();
  }

  @Override
  protected String extractName(Annotation annotation) {
    return ((DataFactoryPlugin) annotation).name();
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
  
	/**
	 * Extract extra classes information from a plugin annotation.
	 * 
	 * @param classMap
	 * @param annotation
	 */
	public void addExtraClasses(Map<Class<?>, String> classMap, Class<?> clazz, Annotation annotation) {
	}
	
  @Override
  protected String extractDocumentationUrl(Annotation annotation) {
    return null;
  }  
  @Override
  protected String extractCasesUrl(Annotation annotation) {
    return null;
  }

  @Override
  protected String extractForumUrl(Annotation annotation) {
    return null;
  }

}
