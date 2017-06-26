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
