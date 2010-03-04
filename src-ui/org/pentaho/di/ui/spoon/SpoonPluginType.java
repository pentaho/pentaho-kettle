package org.pentaho.di.ui.spoon;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.vfs.FileObject;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.plugins.BasePluginType;
import org.pentaho.di.core.plugins.JarFileAnnotationPlugin;
import org.pentaho.di.core.plugins.KettlePluginException;
import org.pentaho.di.core.plugins.Plugin;
import org.pentaho.di.core.plugins.PluginFolder;
import org.pentaho.di.core.plugins.PluginFolderInterface;
import org.pentaho.di.core.plugins.PluginInterface;
import org.pentaho.di.core.plugins.PluginTypeInterface;
import org.pentaho.di.core.util.ResolverUtil;
import org.pentaho.di.job.entry.JobEntryInterface;

public class SpoonPluginType {//extends BasePluginType implements PluginTypeInterface {
//
//  private SpoonPluginType() {
//    super("SPOONPLUGIN", "Spoon Plugin");
//    
//    pluginFolders.add( new PluginFolder("plugins/spoon", false, true) ); 
//  }
//  
//  
//  public String getId() {
//    // TODO Auto-generated method stub
//    return null;
//  }
//
//  public String getName() {
//    // TODO Auto-generated method stub
//    return null;
//  }
//
//
//  public List<PluginFolderInterface> getPluginFolders() {
//    // TODO Auto-generated method stub
//    return null;
//  }
//
//  public void searchPlugins() throws KettlePluginException {
//    // TODO Auto-generated method stub
//    
//  }
//
//  @Override
//  protected List<JarFileAnnotationPlugin> findAnnotatedClassFiles(String annotationClassName) {
//    // TODO Auto-generated method stub
//    return super.findAnnotatedClassFiles(annotationClassName);
//  }
//
//  @Override
//  protected List<FileObject> findPluginXmlFiles(String folder) {
//    // TODO Auto-generated method stub
//    return super.findPluginXmlFiles(folder);
//  }
//
//
//  @Override
//  protected void registerAnnotations() throws KettlePluginException {
//    ResolverUtil<PluginInterface> resolver = new ResolverUtil<PluginInterface>();
//    resolver.findAnnotatedInPackages(SpoonPlugin.class);
//    
//    for (Class<?> clazz : resolver.getClasses())
//    {
//      SpoonPlugin spoonPlugin = clazz.getAnnotation(SpoonPlugin.class);
//      handleAnnotatedClass(clazz, spoonPlugin, new ArrayList<String>(), true);
//    }   
//    
//  }
//
//  @Override
//  protected void registerNatives() throws KettlePluginException {
//    // TODO Auto-generated method stub
//    
//  }
//
//  @Override
//  protected void registerPluginJars() throws KettlePluginException {
//    // TODO Auto-generated method stub
//    
//  }
//
//  @Override
//  protected void registerXmlPlugins() throws KettlePluginException {
//    // TODO Auto-generated method stub
//    
//  }
//
//
//  private void handleAnnotatedClass(Class<?> clazz, SpoonPlugin spoonPlugin, List<String> libraries, boolean nativeJobEntry) throws KettlePluginException {
//    
//    // Only one ID for now
//    String[] ids = new String[] { spoonPlugin.id(), }; 
//    
//    if (ids.length == 1 && Const.isEmpty(ids[0])) { 
//      throw new KettlePluginException("No ID specified for plugin with class: "+clazz.getName());
//    }
//    
//    // The package name to get the descriptions or tool tip from...
//    //
//    String packageName = spoonPlugin.i18nPackageName();
//    if (Const.isEmpty(packageName)) packageName = JobEntryInterface.class.getPackage().getName();
//    
//    // An alternative package to get the description or tool tip from...
//    //
//    String altPackageName = clazz.getPackage().getName();
//    
//    // Determine the i18n descriptions of the step description (name), tool tip and category
//    //
//    String name = getTranslation(spoonPlugin.name(), packageName, altPackageName, clazz);
//    String description = getTranslation(spoonPlugin.description(), packageName, altPackageName, clazz);
//    String category = getTranslation(spoonPlugin.categoryDescription(), packageName, altPackageName, clazz);
//    
//    // Register this step plugin...
//    //
//    Map<Class, String> classMap = new HashMap<Class, String>();
//    classMap.put(SpoonPlugin.class, clazz.getName());
//    
//    PluginInterface stepPlugin = new Plugin(ids, this.getClass(), SpoonPlugin.class, category, description, null, null, false, false, classMap, libraries, null);
//    registry.registerPlugin(this.getClass(), stepPlugin);
//  }

  
}
