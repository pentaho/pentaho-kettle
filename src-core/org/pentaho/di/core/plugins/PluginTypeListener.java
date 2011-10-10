package org.pentaho.di.core.plugins;

/**
 * Listeners can be registered with the plugin registry to receive notifications of plugins being added/remove/modified
 *
 * User: nbaker
 * Date: 11/11/10
 */
public interface PluginTypeListener {
  void pluginAdded(Object serviceObject);
  void pluginRemoved(Object serviceObject);
  void pluginChanged(Object serviceObject);
}
