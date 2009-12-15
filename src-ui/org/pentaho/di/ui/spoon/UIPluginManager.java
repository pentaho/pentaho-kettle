package org.pentaho.di.ui.spoon;

import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.xml.XmlBeanDefinitionStoreException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.FileSystemXmlApplicationContext;

public class UIPluginManager {
  public static UIPluginManager instance = new UIPluginManager();
  private List<SpoonPlugin> plugins = new ArrayList<SpoonPlugin>();
  private final String PLUGIN_FILE_NAME = "plugin.xml";
  
  private UIPluginManager(){
    File dir = new File("plugins/spoon");
    File[] dirChildren = dir.listFiles();
    
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
  
  public static UIPluginManager getInstance(){
    return instance;
  }
  
  
  public List<SpoonPlugin> getPlugins(){
    return plugins;
  }
  
}
