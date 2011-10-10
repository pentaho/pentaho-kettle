/*
 * Copyright (c) 2010 Pentaho Corporation.  All rights reserved. 
 * This software was developed by Pentaho Corporation and is provided under the terms 
 * of the GNU Lesser General Public License, Version 2.1. You may not use 
 * this file except in compliance with the license. If you need a copy of the license, 
 * please go to http://www.gnu.org/licenses/lgpl-2.1.txt. The Original Code is Pentaho 
 * Data Integration.  The Initial Developer is Pentaho Corporation.
 *
 * Software distributed under the GNU Lesser Public License is distributed on an "AS IS" 
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or  implied. Please refer to 
 * the license for the specific language governing your rights and limitations.
 */
package org.pentaho.di.core.lifecycle;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettlePluginException;
import org.pentaho.di.core.logging.LogChannel;
import org.pentaho.di.core.plugins.LifecyclePluginType;
import org.pentaho.di.core.plugins.PluginInterface;
import org.pentaho.di.core.plugins.PluginTypeListener;
import org.pentaho.di.core.plugins.PluginRegistry;

public class LifecycleSupport implements LifecycleListener
{
	private Set<LifecycleListener> lifeListeners;
  private boolean started;
  private LifeEventHandler handler;

	public LifecycleSupport()
	{
		lifeListeners = new HashSet<LifecycleListener>();
    final PluginRegistry registry = PluginRegistry.getInstance();
        List<PluginInterface> plugins = registry.getPlugins(LifecyclePluginType.class);
        for (PluginInterface plugin : plugins) {
        	try {
        		lifeListeners.add( registry.loadClass(plugin, LifecycleListener.class) );
        	} catch(KettleException e) {
        		LogChannel.GENERAL.logError("Unexpected error loading class for plugin "+plugin.getName(), e);
        	}
        }

    registry.addPluginListener(LifecyclePluginType.class, new PluginTypeListener() {
      public void pluginAdded(Object serviceObject) {
        LifecycleListener listener = null;
        try {
          listener = (LifecycleListener) PluginRegistry.getInstance().loadClass((PluginInterface) serviceObject);
        } catch (KettlePluginException e) {
          e.printStackTrace();
          return;
        }

        lifeListeners.add(listener);
        if(started){
          try {
            listener.onStart(handler);
          } catch (LifecycleException e) {
            e.printStackTrace();
          }
        }

      }

      public void pluginRemoved(Object serviceObject) {
        lifeListeners.remove(serviceObject);
      }

      public void pluginChanged(Object serviceObject) {}
    });
	}

	public void onStart(LifeEventHandler handler) throws LifecycleException
	{
    // Caching the last handler and the fact that start has been called. This would cause problems if onStart
    // is called by more than one handler.
    this.handler = handler;
    started = true;
		for (LifecycleListener listener : lifeListeners)
			listener.onStart(handler);
	}

	public void onExit(LifeEventHandler handler) throws LifecycleException
	{
		for (LifecycleListener listener : lifeListeners)
			listener.onExit(handler);
	}
}
