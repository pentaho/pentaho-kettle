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

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.pentaho.di.core.exception.KettlePluginException;
import org.pentaho.di.core.plugins.PluginInterface;
import org.pentaho.di.core.plugins.PluginRegistry;
import org.pentaho.di.ui.spoon.SpoonLifecycleListener.SpoonLifeCycleEvent;
import org.pentaho.ui.xul.XulDomContainer;
import org.pentaho.ui.xul.XulException;
/**
 *  SpoonPluginManager is a singleton class which loads all SpoonPlugins from the 
 *  SPOON_HOME/plugins/spoon directory. 
 *  
 *  Spoon Plugins are able to listen for SpoonLifeCycleEvents and can register categorized 
 *  XUL Overlays to be retrieved later.
 * 
 *  Spoon Plugins are deployed as directories under the SPOON_HOME/plugins/spoon directory. 
 *  Each plugin must provide a build.xml as the root of it's directory and have any required 
 *  jars under a "lib" directory.
 *  
 *  The plugin.xml format is Spring-based e.g.
 *  <beans
 *    xmlns="http://www.springframework.org/schema/beans" 
 *    xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
 *    xsi:schemaLocation="http://www.springframework.org/schema/beans http://www.springframework.org/schema/beans/spring-beans-2.0.xsd">
 *  
 *    <bean id="PLUGIN_ID" class="org.foo.SpoonPluginClassName"></bean>
 *  </beans>
 *  
 * @author nbaker
 */
public class SpoonPluginManager {
  
  private static SpoonPluginManager instance = new SpoonPluginManager();
  private List<SpoonPluginInterface> plugins = new ArrayList<SpoonPluginInterface>();
  
  private Map<String, List<SpoonPluginInterface>> pluginCategoryMap = new HashMap<String, List<SpoonPluginInterface>>();
  
  private SpoonPluginManager(){
    List<PluginInterface> plugins = PluginRegistry.getInstance().getPlugins(SpoonPluginType.class);
    for(PluginInterface plug : plugins){
      try {
        loadPlugin((SpoonPluginInterface) PluginRegistry.getInstance().loadClass(plug));
      } catch (KettlePluginException e) {
        e.printStackTrace();
      }
    }
  }
  
  private void loadPlugin(final SpoonPluginInterface sp){
    SpoonPluginCategories categories = sp.getClass().getAnnotation(SpoonPluginCategories.class);
    if(categories != null){
      for(String cat : categories.value()){
        List<SpoonPluginInterface> categoryList = pluginCategoryMap.get(cat);
        if(categoryList == null){
          categoryList = new ArrayList<SpoonPluginInterface>();
          pluginCategoryMap.put(cat, categoryList);
        }
        categoryList.add(sp);
      }
    }
    
    if(sp.getPerspective() != null){
      SpoonPerspectiveManager.getInstance().addPerspective(sp.getPerspective());
    }

    plugins.add(sp);
  }
  
  /**
   * Return the single instance of this class
   * 
   * @return SpoonPerspectiveManager
   */
  public static SpoonPluginManager getInstance(){
    return instance;
  }
  
  
  public void applyPluginsForContainer(String category, XulDomContainer container) throws XulException{
    List<SpoonPluginInterface> plugins = pluginCategoryMap.get(category);
    if(plugins != null){
      for(SpoonPluginInterface sp : plugins){
        sp.applyToContainer(category, container);
      }
    }
  }
  
  /**
   * Returns an unmodifiable list of all Spoon Plugins.
   * 
   * @return list of plugins
   */
  public List<SpoonPluginInterface> getPlugins(){
    return Collections.unmodifiableList(plugins);
  }
  
  /**
   * Notifies all registered SpoonLifecycleListeners of the given SpoonLifeCycleEvent.
   * 
   * @param evt
   */
  public void notifyLifecycleListeners(SpoonLifeCycleEvent evt){
    for(SpoonPluginInterface p : plugins){
      SpoonLifecycleListener listener = p.getLifecycleListener();
      if(listener != null){
        listener.onEvent(evt);
      }
    }
  }
}
