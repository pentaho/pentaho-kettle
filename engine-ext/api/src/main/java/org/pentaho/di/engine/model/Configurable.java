/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2029-07-20
 ******************************************************************************/

package org.pentaho.di.engine.model;

import com.google.common.collect.ImmutableMap;
import org.pentaho.di.engine.api.HasConfig;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by hudak on 1/18/17.
 */
abstract class Configurable implements HasConfig {
  private static final long serialVersionUID = 5293701152182574661L;
  private final HashMap<String, Serializable> config = new HashMap<>();

  @Override  public Map<String, Serializable> getConfig() {
    return ImmutableMap.copyOf( config );
  }

  @Override public void setConfig( String key, Serializable value ) {
    this.config.put( key, value );
  }

  @Override public void setConfig( Map<String, Serializable> config ) {
    this.config.putAll( config );
  }
}
