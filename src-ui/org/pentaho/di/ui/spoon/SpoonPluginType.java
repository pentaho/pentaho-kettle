package org.pentaho.di.ui.spoon;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.vfs.FileObject;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.annotations.RepositoryPlugin;
import org.pentaho.di.core.exception.KettlePluginException;
import org.pentaho.di.core.plugins.BasePluginType;
import org.pentaho.di.core.plugins.JarFileAnnotationPlugin;
import org.pentaho.di.core.plugins.Plugin;
import org.pentaho.di.core.plugins.PluginClassTypes;
import org.pentaho.di.core.plugins.PluginFolder;
import org.pentaho.di.core.plugins.PluginInterface;
import org.pentaho.di.core.plugins.PluginTypeInterface;
import org.pentaho.di.job.entry.JobEntryInterface;
import org.pentaho.di.repository.Repository;


public class SpoonPluginType extends BasePluginType implements PluginTypeInterface {

  private SpoonPluginType() {
    super("SPOONPLUGIN", "Spoon Plugin");
    
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

  /**
   * Scan & register internal repository type plugins
   */
  protected void registerAnnotations() throws KettlePluginException {

    List<Class<?>> classes = getAnnotatedClasses(RepositoryPlugin.class);
    for (Class<?> clazz : classes)
    {
      SpoonPlugin repositoryPlugin = clazz.getAnnotation(SpoonPlugin.class);
      handleAnnotation(clazz, repositoryPlugin, new ArrayList<String>(), true);
    }   
  }
  

  @Override
  protected void registerNatives() throws KettlePluginException {
    // TODO Auto-generated method stub
    
  }

  protected void registerPluginJars() throws KettlePluginException {
    
    List<JarFileAnnotationPlugin> jarFilePlugins = findAnnotatedClassFiles(SpoonPlugin.class.getName());
    for (JarFileAnnotationPlugin jarFilePlugin : jarFilePlugins) {
      
      URLClassLoader urlClassLoader = new URLClassLoader(new URL[] { jarFilePlugin.getJarFile(), }, getClass().getClassLoader());

      try {
        Class<?> clazz = urlClassLoader.loadClass(jarFilePlugin.getClassFile().getName());
        SpoonPlugin partitioner = clazz.getAnnotation(SpoonPlugin.class);
        List<String> libraries = new ArrayList<String>();
        
        File f = new File(jarFilePlugin.getJarFile().getFile());
        File parent = f.getParentFile();
        File libDir = new File(parent.toString()+File.separator+"lib");;
        if(libDir.exists()){
          for(File fil : libDir.listFiles()){
            if(fil.getName().indexOf(".jar") > 0){
              try {
                libraries.add(fil.toURI().toURL().getFile());
              } catch (MalformedURLException e) {
                e.printStackTrace();
              }
            }
          }
        }

        libraries.add(jarFilePlugin.getJarFile().getFile());
        handleAnnotation(clazz, partitioner, libraries, false);
      } catch(ClassNotFoundException e) {
        // Ignore for now, don't know if it's even possible.
      }
    }
  }
  
  @Override
  protected void registerXmlPlugins() throws KettlePluginException {
    // TODO Auto-generated method stub
    
  }


  private void handleAnnotation(Class<?> clazz, SpoonPlugin spoonPlugin, List<String> libraries, boolean nativeJobEntry) throws KettlePluginException {
    
    // Only one ID for now
    String[] ids = new String[] { spoonPlugin.id(), }; 
    
    if (ids.length == 1 && Const.isEmpty(ids[0])) { 
      throw new KettlePluginException("No ID specified for plugin with class: "+clazz.getName());
    }
    
    // The package name to get the descriptions or tool tip from...
    //
    String packageName = spoonPlugin.i18nPackageName();
    if (Const.isEmpty(packageName)) packageName = JobEntryInterface.class.getPackage().getName();
    
    // An alternative package to get the description or tool tip from...
    //
    String altPackageName = clazz.getPackage().getName();
    
    // Determine the i18n descriptions of the step description (name), tool tip and category
    //
    String name = getTranslation(spoonPlugin.name(), packageName, altPackageName, clazz);
    String description = getTranslation(spoonPlugin.description(), packageName, altPackageName, clazz);
    String category = getTranslation(spoonPlugin.categoryDescription(), packageName, altPackageName, clazz);
    
    // Register this step plugin...
    //
    Map<Class<?>, String> classMap = new HashMap<Class<?>, String>();
    classMap.put(SpoonPlugin.class, clazz.getName());
    
    PluginInterface stepPlugin = new Plugin(ids, this.getClass(), SpoonPlugin.class, category, description, null, null, false, false, classMap, libraries, null);
    registry.registerPlugin(this.getClass(), stepPlugin);
  }

  
}
