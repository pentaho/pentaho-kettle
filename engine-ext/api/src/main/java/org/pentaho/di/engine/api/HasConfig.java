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

package org.pentaho.di.engine.api;

import java.io.Serializable;
import java.util.Map;
import java.util.Optional;

/**
 * I can haz config?
 * Created by hudak on 1/17/17.
 */
public interface HasConfig extends Serializable {
  Map<String, Serializable> getConfig();

  void setConfig( String key, Serializable value );

  default void setConfig( Map<String, Serializable> config ) {
    config.forEach( this::setConfig );
  }

  default Optional<? extends Serializable> getConfig( String key ) {
    return Optional.ofNullable( getConfig().get( key ) );
  }

  default <T> Optional<T> getConfig( String key, Class<T> type ) {
    return getConfig( key ).filter( type::isInstance ).map( type::cast );
  }
}
