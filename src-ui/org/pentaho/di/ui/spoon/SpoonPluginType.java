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

package org.pentaho.di.ui.spoon;

import java.lang.annotation.Annotation;
import java.util.List;
import java.util.Map;

import org.apache.commons.vfs.FileObject;
import org.pentaho.di.core.exception.KettlePluginException;
import org.pentaho.di.core.plugins.BasePluginType;
import org.pentaho.di.core.plugins.JarFileAnnotationPlugin;
import org.pentaho.di.core.plugins.PluginAnnotationType;
import org.pentaho.di.core.plugins.PluginFolder;
import org.pentaho.di.core.plugins.PluginMainClassType;
import org.pentaho.di.core.plugins.PluginTypeInterface;

@PluginMainClassType(SpoonPlugin.class)
@PluginAnnotationType(SpoonPlugin.class)
public class SpoonPluginType extends BasePluginType implements PluginTypeInterface {

  private SpoonPluginType() {
    super(SpoonPlugin.class, "SPOONPLUGIN", "Spoon Plugin");
    
    pluginFolders.add( new PluginFolder("plugins/spoon", false, true) ); 
  }
  
  private static SpoonPluginType pluginType;
  public static SpoonPluginType getInstance() {
    if (pluginType==null) {
      pluginType = new SpoonPluginType();
    }
    return pluginType;
  }
    @Override
  protected List<JarFileAnnotationPlugin> findAnnotatedClassFiles(String annotationClassName) {
    // TODO Auto-generated method stub
    return super.findAnnotatedClassFiles(annotationClassName);
  }

  @Override
  protected List<FileObject> findPluginXmlFiles(String folder) {
    // TODO Auto-generated method stub
    return super.findPluginXmlFiles(folder);
  }


  @Override
  protected void registerNatives() throws KettlePluginException {
    // TODO Auto-generated method stub
  }

  
  @Override
  protected void registerXmlPlugins() throws KettlePluginException {
    // TODO Auto-generated method stub
  }


  @Override
  protected String extractCategory(Annotation annotation) {
    return ((SpoonPlugin) annotation).categoryDescription();
  }

  @Override
  protected String extractDesc(Annotation annotation) {
    return ((SpoonPlugin) annotation).description();
  }

  @Override
  protected String extractID(Annotation annotation) {
    return ((SpoonPlugin) annotation).id();
  }

  @Override
  protected String extractName(Annotation annotation) {
    return ((SpoonPlugin) annotation).name();
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
    return ((SpoonPlugin) annotation).i18nPackageName();
  }

  @Override
  protected void addExtraClasses(Map<Class<?>, String> classMap, Class<?> clazz, Annotation annotation) {	  
  }

}
