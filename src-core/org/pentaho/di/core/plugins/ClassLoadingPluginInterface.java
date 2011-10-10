package org.pentaho.di.core.plugins;

/**
 *
 * Plugins implementing this type will be asked to load classes instead of having it handled by the PluginRegistry.
 *
 * User: nbaker
 * Date: 12/12/10
 */
public interface ClassLoadingPluginInterface {
  <T> T loadClass(Class<T> pluginClass);
  ClassLoader getClassLoader();
}
