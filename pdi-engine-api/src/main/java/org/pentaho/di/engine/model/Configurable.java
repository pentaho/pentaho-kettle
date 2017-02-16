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
  private final HashMap<String, Serializable> config = new HashMap<>();

  @Override  public Map<String, Serializable> getConfig() {
    return ImmutableMap.copyOf( config );
  }

  public void setConfig( String key, Serializable value ) {
    this.config.put( key, value );
  }

  public void setConfig( Map<String, Serializable> config ) {
    this.config.putAll( config );
  }
}
