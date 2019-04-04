/*
 * *****************************************************************************
 *
 *  Pentaho Data Integration
 *
 *  Copyright (C) 2002-2017 by Hitachi Vantara : http://www.pentaho.com
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

package org.pentaho.di.engine.configuration.impl.spark;

import org.pentaho.di.engine.configuration.api.RunConfiguration;
import org.pentaho.di.engine.configuration.api.RunConfigurationExecutor;
import org.pentaho.di.engine.configuration.api.RunConfigurationProvider;
import org.pentaho.di.engine.configuration.impl.MetaStoreRunConfigurationFactory;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.metastore.api.exceptions.MetaStoreException;
import org.pentaho.metastore.persist.MetaStoreFactory;
import org.pentaho.osgi.metastore.locator.api.MetastoreLocator;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Created by bmorrise on 3/16/17.
 */
public class SparkRunConfigurationProvider extends MetaStoreRunConfigurationFactory
  implements RunConfigurationProvider {

  public static String TYPE = "Spark";
  private SparkRunConfigurationExecutor sparkRunConfigurationExecutor;
  private List<String> supported = Arrays.asList( TransMeta.XML_TAG );

  public SparkRunConfigurationProvider( MetastoreLocator metastoreLocator,
                                        SparkRunConfigurationExecutor sparkRunConfigurationExecutor ) {
    super( metastoreLocator );
    this.sparkRunConfigurationExecutor = sparkRunConfigurationExecutor;
  }

  @Override public RunConfiguration getConfiguration() {
    return new SparkRunConfiguration();
  }

  @Override public String getType() {
    return TYPE;
  }

  @SuppressWarnings( "unchecked" )
  protected MetaStoreFactory<SparkRunConfiguration> getMetaStoreFactory() throws MetaStoreException {
    return getMetastoreFactory( SparkRunConfiguration.class );
  }

  @Override public RunConfigurationExecutor getExecutor() {
    return sparkRunConfigurationExecutor;
  }

  @Override public boolean isSupported( String type ) {
    return supported.contains( type );
  }

  @Override public List<String> getNames( String type ) {
    return isSupported( type ) ? getNames() : Collections.emptyList();
  }
}
