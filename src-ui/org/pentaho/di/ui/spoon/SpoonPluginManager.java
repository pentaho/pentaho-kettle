package org.pentaho.di.ui.spoon;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.pentaho.di.core.exception.KettlePluginException;
import org.pentaho.di.core.plugins.PluginInterface;
import org.pentaho.di.core.plugins.PluginRegistry;
import org.pentaho.di.ui.spoon.SpoonLifecycleListener.SpoonLifeCycleEvent;
import org.pentaho.ui.xul.XulOverlay;
import org.pentaho.ui.xul.impl.XulEventHandler;
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
  private final String PLUGIN_FILE_NAME = "plugin.xml";
  
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
  
  @SuppressWarnings("unchecked")
  private void loadPlugin(final SpoonPluginInterface sp){
    plugins.add(sp);
    if(sp.getPerspective() != null){
      SpoonPerspectiveManager.getInstance().addPerspective(sp.getPerspective());
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
  public List<SpoonPluginInterface> getPlugins(){
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
    
    for(SpoonPluginInterface p : plugins){
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
    
    for(SpoonPluginInterface p : plugins){
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
    for(SpoonPluginInterface p : plugins){
      SpoonLifecycleListener listener = p.getLifecycleListener();
      if(listener != null){
        listener.onEvent(evt);
      }
    }
  }
}
