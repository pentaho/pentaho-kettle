package org.pentaho.di.core.extension;

import java.util.List;

import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.logging.LogChannelInterface;
import org.pentaho.di.core.plugins.PluginInterface;
import org.pentaho.di.core.plugins.PluginRegistry;

public class ExtensionPointHandler {

  /**
   * This method looks up the extension point plugins with the given ID in the plugin registry.
   * If one or more are found, their corresponding interfaces are instantiated and the callExtensionPoint() method is invoked.
   *  
   * @param log the logging channel to write debugging information to
   * @param id The ID of the extension point to call
   * @param object The parent object that is passed to the plugin
   * @throws KettleException In case something goes wrong in the plugin and we need to stop what we're doing.
   */
  public static void callExtensionPoint(final LogChannelInterface log, final String id, final Object object) throws KettleException {
    PluginRegistry registry = PluginRegistry.getInstance();
    List<PluginInterface> extensionPointPlugins = registry.getPlugins(ExtensionPointPluginType.class);
    for (PluginInterface extensionPointPlugin : extensionPointPlugins) {
      if (id.equals(extensionPointPlugin.getName())) {
        ExtensionPointInterface extensionPoint = (ExtensionPointInterface) registry.loadClass(extensionPointPlugin);
        log.logDetailed("Handling extension point for plugin with id '"+extensionPointPlugin.getIds()[0]+"' and extension point id '"+id+"'");
        extensionPoint.callExtensionPoint(log, object);
      }
    }
  }
}
