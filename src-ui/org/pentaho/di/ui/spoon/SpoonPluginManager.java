package org.pentaho.di.ui.spoon;

import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.pentaho.di.ui.spoon.SpoonLifecycleListener.LifeCycleEvent;
import org.pentaho.ui.xul.XulOverlay;
import org.pentaho.ui.xul.impl.XulEventHandler;
import org.springframework.beans.factory.xml.XmlBeanDefinitionStoreException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.FileSystemXmlApplicationContext;

public class SpoonPluginManager {
  public static SpoonPluginManager instance = new SpoonPluginManager();
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
  
  public static SpoonPluginManager getInstance(){
    return instance;
  }
  
  
  public List<SpoonPlugin> getPlugins(){
    return plugins;
  }
  
  public List<XulOverlay> getOverlaysforContainer(String id){
    List<XulOverlay> overlays = new ArrayList<XulOverlay>();
    
    for(SpoonPlugin p : plugins){
      if(p.getOverlays() == null){
        continue;
      }
      if(p.getOverlays().containsKey(id)){
        overlays.add(p.getOverlays().get(id));
      }
    }
    return overlays;
  }

  public List<XulEventHandler> getEventHandlersforContainer(String id){
    List<XulEventHandler> handlers = new ArrayList<XulEventHandler>();
    
    for(SpoonPlugin p : plugins){
      if(p.getEventHandlers() == null){
        continue;
      }
      if(p.getEventHandlers().containsKey(id)){
        handlers.add(p.getEventHandlers().get(id));
      }
    }
    return handlers;
  }
  
  public void notifyLifecycleListeners(LifeCycleEvent evt){
    for(SpoonPlugin p : plugins){
      SpoonLifecycleListener listener = p.getLifecycleListener();
      if(listener != null){
        listener.onEvent(evt);
      }
    }
  }
}
