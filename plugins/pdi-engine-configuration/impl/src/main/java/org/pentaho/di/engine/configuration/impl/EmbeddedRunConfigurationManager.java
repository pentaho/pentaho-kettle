/*
 * *****************************************************************************
 *
 *  Pentaho Data Integration
 *
 *  Copyright (C) 2002-2017 by Pentaho : http://www.pentaho.com
 *
 *  *******************************************************************************
 *  Licensed under the Apache License, Version 2.0 (the "License"); you may not use
 *  this file except in compliance with the License. You may obtain a copy of the
 *  License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 * *****************************************************************************
 *
 */

package org.pentaho.di.engine.configuration.impl;

import org.pentaho.di.core.attributes.metastore.EmbeddedMetaStore;
import org.pentaho.di.engine.configuration.api.RunConfigurationProvider;
import org.pentaho.di.engine.configuration.impl.pentaho.DefaultRunConfigurationProvider;
import org.pentaho.di.engine.configuration.impl.spark.SparkRunConfigurationProvider;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by bmorrise on 5/4/17.
 */
public class EmbeddedRunConfigurationManager {
  public static RunConfigurationManager build( EmbeddedMetaStore embeddedMetaStore ) {
    DefaultRunConfigurationProvider defaultRunConfigurationProvider =
      new DefaultRunConfigurationProvider( () -> embeddedMetaStore, null );
    SparkRunConfigurationProvider sparkRunConfigurationProvider =
      new SparkRunConfigurationProvider( () -> embeddedMetaStore, null );

    List<RunConfigurationProvider> runConfigurationProviders = new ArrayList<>();
    runConfigurationProviders.add( defaultRunConfigurationProvider );
    runConfigurationProviders.add( sparkRunConfigurationProvider );

    return new RunConfigurationManager( runConfigurationProviders );
  }
}
