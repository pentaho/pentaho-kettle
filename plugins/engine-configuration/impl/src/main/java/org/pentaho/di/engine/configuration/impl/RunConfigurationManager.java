/*
 * *****************************************************************************
 *
 *  Pentaho Data Integration
 *
 *  Copyright (C) 2002-2024 by Hitachi Vantara : http://www.pentaho.com
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

import org.pentaho.di.core.bowl.Bowl;
import org.pentaho.di.core.bowl.DefaultBowl;
import org.pentaho.di.engine.configuration.api.RunConfiguration;
import org.pentaho.di.engine.configuration.api.RunConfigurationExecutor;
import org.pentaho.di.engine.configuration.api.RunConfigurationProvider;
import org.pentaho.di.engine.configuration.api.RunConfigurationService;
import org.pentaho.di.engine.configuration.impl.pentaho.DefaultRunConfigurationProvider;
import org.pentaho.di.metastore.MetaStoreConst;
import org.pentaho.metastore.api.IMetaStore;

import java.util.ArrayList;
import java.util.Collections;
import java.util.function.Supplier;
import java.util.List;

/**
 * Created by bmorrise on 3/14/17.
 */
public class RunConfigurationManager implements RunConfigurationService {

  private RunConfigurationProvider defaultRunConfigurationProvider;
  private List<RunConfigurationProvider> runConfigurationProviders = new ArrayList<>();
  private static RunConfigurationManager instance;

  public static RunConfigurationManager getInstance() {
    if ( null == instance ) {
      instance = new RunConfigurationManager();
    }
    return instance;
  }

  public static RunConfigurationManager getInstance( Bowl bowl ) {

    CheckedMetaStoreSupplier bowlSupplier = () -> bowl != null ? bowl.getMetastore() :
        DefaultBowl.getInstance().getMetastore();
    RunConfigurationProvider provider = new DefaultRunConfigurationProvider( bowlSupplier );
    return  new RunConfigurationManager( Collections.singletonList( provider ) );
  }

  public RunConfigurationManager( List<RunConfigurationProvider> runConfigurationProviders ) {
    this.runConfigurationProviders = runConfigurationProviders;
  }

  private RunConfigurationManager() {
    this.defaultRunConfigurationProvider = new DefaultRunConfigurationProvider();
  }

  @Override public List<RunConfiguration> load() {
    List<RunConfiguration> runConfigurations = new ArrayList<>();
    for ( RunConfigurationProvider runConfigurationProvider : getRunConfigurationProviders() ) {
      runConfigurations.addAll( runConfigurationProvider.load() );
    }
    Collections.sort( runConfigurations, ( o1, o2 ) -> {
      if ( o2.getName().equals( DefaultRunConfigurationProvider.DEFAULT_CONFIG_NAME ) ) {
        return 1;
      }
      return o1.getName().compareToIgnoreCase( o2.getName() );
    } );
    return runConfigurations;
  }

  @Override public RunConfiguration load( String name ) {
    for ( RunConfigurationProvider runConfigurationProvider : getRunConfigurationProviders() ) {
      RunConfiguration runConfiguration = runConfigurationProvider.load( name );
      if ( runConfiguration != null ) {
        return runConfiguration;
      }
    }
    return null;
  }

  @Override
  public boolean save( RunConfiguration runConfiguration ) {
    RunConfigurationProvider runConfigurationProvider = runConfiguration != null ? getProvider( runConfiguration.getType() ) : null;
    return runConfigurationProvider != null && runConfigurationProvider.save( runConfiguration );
  }


  @Override
  public boolean delete( String name ) {
    for ( RunConfigurationProvider runConfigurationProvider : getRunConfigurationProviders() ) {
      if ( runConfigurationProvider.load( name ) != null && runConfigurationProvider.delete( name ) ) {
        return true;
      }
    }
    return false;
  }

  @Override public void deleteAll() {
    for ( RunConfigurationProvider runConfigurationProvider : getRunConfigurationProviders() ) {
      runConfigurationProvider.deleteAll();
    }
  }

  public String[] getTypes() {
    List<String> types = new ArrayList<>();
    for ( RunConfigurationProvider runConfigurationProvider : getRunConfigurationProviders() ) {
      types.add( runConfigurationProvider.getType() );
    }
    return types.toArray( new String[ 0 ] );
  }

  public List<String> getNames() {
    List<String> names = new ArrayList<>();
    for ( RunConfigurationProvider runConfigurationProvider : getRunConfigurationProviders() ) {
      names.addAll( runConfigurationProvider.getNames() );
    }
    Collections.sort( names, ( o1, o2 ) -> {
      if ( o2.equals( DefaultRunConfigurationProvider.DEFAULT_CONFIG_NAME ) ) {
        return 1;
      }
      return o1.compareToIgnoreCase( o2 );
    } );
    return names;
  }

  public List<String> getNames( String type ) {
    List<String> names = new ArrayList<>();
    for ( RunConfigurationProvider runConfigurationProvider : getRunConfigurationProviders( type ) ) {
      names.addAll( runConfigurationProvider.getNames() );
    }
    Collections.sort( names, ( o1, o2 ) -> {
      if ( o2.equals( DefaultRunConfigurationProvider.DEFAULT_CONFIG_NAME ) ) {
        return 1;
      }
      return o1.compareToIgnoreCase( o2 );
    } );
    return names;
  }


  public RunConfiguration getRunConfigurationByType( String type ) {
    RunConfigurationProvider runConfigurationProvider = getProvider( type );
    if ( runConfigurationProvider != null ) {
      return runConfigurationProvider.getConfiguration();
    }
    return null;
  }

  public RunConfigurationExecutor getExecutor( String type ) {
    RunConfigurationProvider runConfigurationProvider = getProvider( type );
    if ( runConfigurationProvider != null ) {
      return runConfigurationProvider.getExecutor();
    }
    return null;
  }

  private RunConfigurationProvider getProvider( String type ) {
    for ( RunConfigurationProvider runConfigurationProvider : getRunConfigurationProviders() ) {
      if ( runConfigurationProvider.getType().equals( type ) ) {
        return runConfigurationProvider;
      }
    }
    return null;
  }

  public List<RunConfigurationProvider> getRunConfigurationProviders( String type ) {
    List<RunConfigurationProvider> runConfigurationProviders = new ArrayList<>();
    if ( defaultRunConfigurationProvider != null ) {
      runConfigurationProviders.add( defaultRunConfigurationProvider );
    }
    for ( RunConfigurationProvider runConfigurationProvider : this.runConfigurationProviders ) {
      if ( runConfigurationProvider.isSupported( type ) ) {
        runConfigurationProviders.add( runConfigurationProvider );
      }
    }
    return runConfigurationProviders;
  }

  public List<RunConfigurationProvider> getRunConfigurationProviders() {
    List<RunConfigurationProvider> runConfigurationProviders = new ArrayList<>();
    if ( defaultRunConfigurationProvider != null ) {
      runConfigurationProviders.add( defaultRunConfigurationProvider );
    }
    runConfigurationProviders.addAll( this.runConfigurationProviders );
    return runConfigurationProviders;
  }

  public RunConfigurationProvider getDefaultRunConfigurationProvider() {
    return defaultRunConfigurationProvider;
  }

  public void setDefaultRunConfigurationProvider(
    RunConfigurationProvider defaultRunConfigurationProvider ) {
    this.defaultRunConfigurationProvider = defaultRunConfigurationProvider;
  }
}
