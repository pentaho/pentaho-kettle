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

import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.logging.LogChannel;
import org.pentaho.di.core.plugins.LifecyclePluginType;
import org.pentaho.di.core.plugins.PluginInterface;
import org.pentaho.di.core.plugins.PluginRegistry;

public class LifecycleSupport implements LifecycleListener
{
	private Set<LifecycleListener> lifeListeners;

	public LifecycleSupport()
	{
		lifeListeners = new HashSet<LifecycleListener>();
        PluginRegistry registry = PluginRegistry.getInstance();
        List<PluginInterface> plugins = registry.getPlugins(LifecyclePluginType.class);
        for (PluginInterface plugin : plugins) {
        	try {
        		lifeListeners.add( registry.loadClass(plugin, LifecycleListener.class) );
        	} catch(KettleException e) {
        		LogChannel.GENERAL.logError("Unexpected error loading class for plugin "+plugin.getName(), e);
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
