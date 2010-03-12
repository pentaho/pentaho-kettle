/**
 * 
 */
package org.pentaho.di.core.plugins;


import java.lang.annotation.Annotation;

import org.pentaho.di.core.annotations.LifecyclePlugin;
import org.pentaho.di.core.exception.KettlePluginException;
import org.pentaho.di.core.gui.GUIOption;
import org.pentaho.di.core.lifecycle.LifecycleListener;

/**
 * This class represents the repository plugin type.
 * 
 * @author matt
 * 
 */
@PluginMainClassType(LifecycleListener.class)
@PluginExtraClassTypes(classTypes = { GUIOption.class })
public class LifecyclePluginType extends BasePluginType implements
    PluginTypeInterface {

  private static LifecyclePluginType pluginType;

  private LifecyclePluginType() {
    super(LifecyclePlugin.class, "LIFECYCLE LISTENERS", "Lifecycle listener plugin type");
    populateFolders("repositories");
  }

  public static LifecyclePluginType getInstance() {
    if (pluginType == null) {
      pluginType = new LifecyclePluginType();
    }
    return pluginType;
  }

  /**
   * Let's put in code here to search for the step plugins..
   */
  public void searchPlugins() throws KettlePluginException {
    registerNatives(); // none
    registerAnnotations(); // no longer performed
    registerPluginJars();
    registerXmlPlugins();
  }

  /**
   * Scan & register internal step plugins
   */
  protected void registerNatives() throws KettlePluginException {
    // Up until now, we have no natives.
  }

  /**
   * Scan & register internal repository type plugins
   */
  protected void registerAnnotations() throws KettlePluginException {
    // This is no longer done because it was deemed too slow. Only jar files in
    // the plugins/ folders are scanned for annotations.
  }

  protected void registerXmlPlugins() throws KettlePluginException {
    // Not supported yet.
  }

  @Override
  protected String extractCategory(Annotation annotation) {
    return "";
  }

  @Override
  protected String extractDesc(Annotation annotation) {
    return "";
  }

  @Override
  protected String extractID(Annotation annotation) {
    return ((LifecyclePlugin) annotation).id();
  }

  @Override
  protected String extractName(Annotation annotation) {
    return ((LifecyclePlugin) annotation).name();
  }


}
