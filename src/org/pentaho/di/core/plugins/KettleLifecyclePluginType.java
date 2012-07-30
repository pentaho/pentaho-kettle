package org.pentaho.di.core.plugins;

import java.lang.annotation.Annotation;
import java.util.Map;

import org.pentaho.di.core.annotations.KettleLifecyclePlugin;
import org.pentaho.di.core.exception.KettlePluginException;
import org.pentaho.di.core.lifecycle.KettleLifecycleListener;

/**
 * Defines a Kettle Environment lifecycle plugin type. These plugins are invoked
 * at Kettle Environment initialization and shutdown.
 */
@PluginMainClassType(KettleLifecycleListener.class)
@PluginAnnotationType(KettleLifecyclePlugin.class)
public class KettleLifecyclePluginType extends BasePluginType implements PluginTypeInterface {

  private static KettleLifecyclePluginType pluginType;

  private KettleLifecyclePluginType() {
    super(KettleLifecyclePlugin.class, "KETTLE LIFECYCLE LISTENERS", "Kettle Lifecycle Listener Plugin Type");
    // We must call populate folders so PluginRegistry will look in the correct 
    // locations for plugins (jars with annotations)
    populateFolders(null);
  }

  public static synchronized KettleLifecyclePluginType getInstance() {
    if (pluginType == null) {
      pluginType = new KettleLifecyclePluginType();
    }
    return pluginType;
  }

  @Override
  protected void registerNatives() throws KettlePluginException {
    // No natives yet
  }

  @Override
  protected void registerXmlPlugins() throws KettlePluginException {
    // Not supported
  }

  @Override
  protected String extractID(Annotation annotation) {
    return ((KettleLifecyclePlugin) annotation).id();
  }

  @Override
  protected String extractName(Annotation annotation) {
    return ((KettleLifecyclePlugin) annotation).name();
  }

  @Override
  protected String extractDesc(Annotation annotation) {
    return "";
  }

  @Override
  protected String extractCategory(Annotation annotation) {
    // No images, not shown in UI
    return "";
  }

  @Override
  protected String extractImageFile(Annotation annotation) {
    // No images, not shown in UI
    return null;
  }

  @Override
  protected boolean extractSeparateClassLoader(Annotation annotation) {
    return ((KettleLifecyclePlugin) annotation).isSeparateClassLoaderNeeded();
  }

  @Override
  protected String extractI18nPackageName(Annotation annotation) {
    // No UI, no i18n
    return null;
  }

  @Override
  protected void addExtraClasses(Map<Class<?>, String> classMap, Class<?> clazz, Annotation annotation) {
    classMap.put(KettleLifecyclePlugin.class, clazz.getName());
  }
}
