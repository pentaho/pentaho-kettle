/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2028-08-13
 ******************************************************************************/


package org.pentaho.di.core.plugins;

import org.pentaho.di.core.exception.KettlePluginException;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;

/**
 * This is a holder of Plugin Class mappings which supplement those of the stock Plugin.
 * <p>
 * Created by nbaker on 3/17/17.
 */
public class SupplementalPlugin extends Plugin implements ClassLoadingPluginInterface {
  Map<Class, Callable> factoryMap = new HashMap<>();
  private Class<? extends PluginTypeInterface> pluginClass;
  private String id;

  public SupplementalPlugin( Class<? extends PluginTypeInterface> pluginClass, String id ) {
    super( new String[] { id }, pluginClass, null,
      "", id, id, "", false, false, Collections.emptyMap(), Collections.emptyList(), "",
      null );
    this.pluginClass = pluginClass;
    this.id = id;
  }

  @Override public <T> T loadClass( Class<T> pluginClass ) {
    if ( !factoryMap.containsKey( pluginClass ) ) {
      return null;
    }

    try {
      return (T) factoryMap.get( pluginClass ).call();
    } catch ( Exception e ) {
      throw new RuntimeException( new KettlePluginException( "Error creating plugin class", e ) );
    }
  }

  @Override public ClassLoader getClassLoader() {
    return getClass().getClassLoader();
  }

  public <T> void addFactory( Class<T> tClass, Callable<T> callable ) {
    factoryMap.put( tClass, callable );
  }

}
