package org.pentaho.di.engine.model;

import com.google.common.collect.ImmutableMap;
import org.pentaho.di.engine.api.IHasConfig;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by hudak on 1/18/17.
 */
abstract class Configurable implements IHasConfig {
  private final HashMap<String, Object> config = new HashMap<>();

  @Override public Map<String, Object> getConfig() {
    return ImmutableMap.copyOf( config );
  }

  public void setConfig( String key, Object value ) {
    this.config.put( key, value );
  }

  public void setConfig( Map<String, Object> config ) {
    this.config.putAll( config );
  }
}
