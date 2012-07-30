/*******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2012 by Pentaho : http://www.pentaho.com
 *
 *******************************************************************************
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 ******************************************************************************/

package org.pentaho.di.core.lifecycle;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.pentaho.di.core.Const;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.logging.LogChannel;
import org.pentaho.di.core.plugins.KettleLifecyclePluginType;
import org.pentaho.di.core.plugins.LifecyclePluginType;
import org.pentaho.di.core.plugins.PluginInterface;
import org.pentaho.di.core.plugins.PluginRegistry;
import org.pentaho.di.core.plugins.PluginTypeInterface;
import org.pentaho.di.i18n.BaseMessages;

public class LifecycleSupport
{
  private static Class<?> PKG = Const.class; // for i18n purposes, needed by Translator2!!   $NON-NLS-1$

	private Set<LifecycleListener> lifeListeners;
	private Set<KettleLifecycleListener> kettleLifecycleListeners;

	public LifecycleSupport()
	{
		lifeListeners = loadPlugins(LifecyclePluginType.class, LifecycleListener.class);
    kettleLifecycleListeners = loadPlugins(KettleLifecyclePluginType.class, KettleLifecycleListener.class); 
	}
	
	/**
	 * Instantiate the main plugin class types for the plugin type provided from
	 * the set of registered plugins via {@link PluginRegistry}.
	 *
	 * @param pluginType Type of plugin whose main class types should be instanticated
	 * @return Set of plugin main class instances (a.k.a. plugins)
	 */
	protected <T> Set<T> loadPlugins(Class<? extends PluginTypeInterface> pluginType, Class<T> mainPluginClass) {
	  Set<T> pluginInstances = new HashSet<T>();
    PluginRegistry registry = PluginRegistry.getInstance();
    List<PluginInterface> plugins = registry.getPlugins(pluginType);
    for (PluginInterface plugin : plugins) {
      try {
        pluginInstances.add(registry.loadClass(plugin, mainPluginClass));
      } catch(Throwable e) {
        LogChannel.GENERAL.logError("Unexpected error loading class for plugin "+plugin.getName(), e);
      }
    }
    return pluginInstances;
	}

  /**
   * Execute all known listener's {@link #onEnvironmentInit()} methods. If an 
   * invocation throws a {@link LifecycleException} is severe this method will 
   * re-throw the exception.
   * 
   * @throws LifecycleException if any listener throws a severe Lifecycle Exception or any {@link Throwable}.
   */
  public void onEnvironmentInit() throws KettleException {
    for (KettleLifecycleListener listener : kettleLifecycleListeners) {
      try {
        listener.onEnvironmentInit();
      } catch (LifecycleException ex) {
        String message = BaseMessages.getString(PKG, "LifecycleSupport.ErrorInvokingKettleLifecycleListener", listener);
        if (ex.isSevere()) {
          throw new KettleException(message, ex);
        }
        // Not a severe error so let's simply log it and continue invoking the others
        LogChannel.GENERAL.logError(message, ex);
      } catch (Throwable t) {
        throw new KettleException(BaseMessages.getString(PKG, "LifecycleSupport.ErrorInvokingKettleLifecycleListener",
            listener), t);
      }
    }
  }

  public void onEnvironmentShutdown()
	{
    for (KettleLifecycleListener listener : kettleLifecycleListeners) {
      try {
        listener.onEnvironmentShutdown();
      } catch (Throwable t) {
        // Log the error and continue invoking other listeners
        LogChannel.GENERAL.logError(BaseMessages.getString(PKG, "LifecycleSupport.ErrorInvokingKettleLifecycleListener", listener), t);
      }
    }
	}
	
	public void onStart(LifeEventHandler handler) throws LifecycleException
	{
		for (LifecycleListener listener : lifeListeners)
			listener.onStart(handler);
	}

	public void onExit(LifeEventHandler handler) throws LifecycleException
	{
		for (LifecycleListener listener : lifeListeners)
			listener.onExit(handler);
	}
}
