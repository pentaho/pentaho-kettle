package org.pentaho.di.ui.spoon;

import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.pentaho.di.ui.spoon.SpoonLifecycleListener.SpoonLifeCycleEvent;
import org.pentaho.ui.xul.XulOverlay;
import org.pentaho.ui.xul.impl.XulEventHandler;
import org.springframework.beans.factory.xml.XmlBeanDefinitionStoreException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.FileSystemXmlApplicationContext;

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
  private List<SpoonPlugin> plugins = new ArrayList<SpoonPlugin>();
  private final String PLUGIN_FILE_NAME = "plugin.xml";
  
  private SpoonPluginManager(){
    File dir = new File("plugins/spoon");
    File[] dirChildren = dir.listFiles();
    if(dirChildren == null || dirChildren.length == 0){
      return;
    }
    final FileFilter pluginFileFilter = new FileFilter(){
      public boolean accept(File f) {
        return f.getName().equals(PLUGIN_FILE_NAME);
      }
    };
    
    for(int i=0; i< dirChildren.length; i++){
      if(dirChildren[i].isDirectory()){
        File pluginDir = dirChildren[i];
        File[] pluginFiles = pluginDir.listFiles(pluginFileFilter);
        if(pluginFiles != null && pluginFiles.length > 0){
          loadPlugin(pluginFiles[0]);
        }
      }
    }
  }
  
  @SuppressWarnings("unchecked")
  private void loadPlugin(final File pluginFile){
    try {
      ApplicationContext context = new FileSystemXmlApplicationContext(pluginFile.getPath());
      Map beans = context.getBeansOfType(SpoonPlugin.class);
      for (Object key : beans.keySet()) {
        SpoonPlugin plg = (SpoonPlugin)beans.get(key);
        plugins.add(plg);
        if(plg.getPerspective() != null){
          SpoonPerspectiveManager.getInstance().addPerspective(plg.getPerspective());
        }
      }
    } catch (XmlBeanDefinitionStoreException e) {
      e.printStackTrace();
    }
    
  }
  
  /**
   * Return the single instance of this class
   * 
   * @return SpoonPerspectiveManager
   */
  public static SpoonPluginManager getInstance(){
    return instance;
  }
  
  /**
   * Returns an unmodifiable list of all Spoon Plugins.
   * 
   * @return list of plugins
   */
  public List<SpoonPlugin> getPlugins(){
    return Collections.unmodifiableList(plugins);
  }
  
  /**
   * Returns a list of Overlays registered for the given category 
   * 
   * @param category Predefined Overlay category e.g. "spoon", "databaseDialog"
   * @return list of XulOverlays
   */
  public List<XulOverlay> getOverlaysforContainer(String category){
    List<XulOverlay> overlays = new ArrayList<XulOverlay>();
    
    for(SpoonPlugin p : plugins){
      if(p.getOverlays() == null){
        continue;
      }
      if(p.getOverlays().containsKey(category)){
        for(XulOverlay o : p.getOverlays().get(category)){
          overlays.add(o);
        }
      }
    }
    return overlays;
  }

  /** 
   * Returns a list of XulEventHandlers registered for the given category.
   * 
   * @param category Predefined Overlay category e.g. "spoon", "databaseDialog"
   * @return list of XulEventHandlers
   */
  public List<XulEventHandler> getEventHandlersforContainer(String category){
    List<XulEventHandler> handlers = new ArrayList<XulEventHandler>();
    
    for(SpoonPlugin p : plugins){
      if(p.getEventHandlers() == null){
        continue;
      }
      if(p.getEventHandlers().containsKey(category)){
        for(XulEventHandler h : p.getEventHandlers().get(category)){
          handlers.add(h);
        }
      }
    }
    return handlers;
  }
  
  /**
   * Notifies all registered SpoonLifecycleListeners of the given SpoonLifeCycleEvent.
   * 
   * @param evt
   */
  public void notifyLifecycleListeners(SpoonLifeCycleEvent evt){
    for(SpoonPlugin p : plugins){
      SpoonLifecycleListener listener = p.getLifecycleListener();
      if(listener != null){
        listener.onEvent(evt);
      }
    }
  }
}
