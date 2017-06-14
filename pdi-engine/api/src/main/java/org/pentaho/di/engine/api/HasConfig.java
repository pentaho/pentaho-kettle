/*
 * ! ******************************************************************************
 *
 *  Pentaho Data Integration
 *
 *  Copyright (C) 2002-2017 by Pentaho : http://www.pentaho.com
 *
 * ******************************************************************************
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with
 *  the License. You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 * *****************************************************************************
 */

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
