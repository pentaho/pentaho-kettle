package org.pentaho.di.engine.api;

import java.util.Map;
import java.util.Optional;

/**
 * I can haz config?
 * Created by hudak on 1/17/17.
 */
public interface HasConfig {
  Map<String, Object> getConfig();

  default Optional<Object> getConfig( String key ) {
    return Optional.ofNullable( getConfig().get( key ) );
  }

  default <T> Optional<T> getConfig( String key, Class<T> type ) {
    return getConfig( key ).filter( type::isInstance ).map( type::cast );
  }
}
