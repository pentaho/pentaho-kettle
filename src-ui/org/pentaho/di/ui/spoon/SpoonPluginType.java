/*
 * Copyright (c) 2010 Pentaho Corporation.  All rights reserved. 
 * This software was developed by Pentaho Corporation and is provided under the terms 
 * of the GNU Lesser General Public License, Version 2.1. You may not use 
 * this file except in compliance with the license. If you need a copy of the license, 
 * please go to http://www.gnu.org/licenses/lgpl-2.1.txt. The Original Code is Pentaho 
 * Data Integration.  The Initial Developer is Pentaho Corporation.
 *
 * Software distributed under the GNU Lesser Public License is distributed on an "AS IS" 
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or  implied. Please refer to 
 * the license for the specific language governing your rights and limitations.
 */
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
    
    pluginFolders.add( new PluginFolder("plugins", false, true) ); 
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
