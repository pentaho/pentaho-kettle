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


package org.pentaho.di.engine.configuration.impl;

import org.pentaho.di.engine.configuration.api.CheckedMetaStoreSupplier;
import org.pentaho.di.engine.configuration.api.RunConfiguration;
import org.pentaho.di.engine.configuration.api.RunConfigurationExecutor;
import org.pentaho.di.engine.configuration.api.RunConfigurationProvider;
import org.pentaho.di.engine.configuration.api.RunConfigurationService;
import org.pentaho.di.engine.configuration.impl.pentaho.DefaultRunConfigurationProvider;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by bmorrise on 3/14/17.
 */
public class RunConfigurationManager implements RunConfigurationService {

  private final List<RunConfigurationProvider> runConfigurationProviders;
  private static RunConfigurationManager instance;

  public static RunConfigurationManager getInstance( CheckedMetaStoreSupplier bowlSupplier ) {
    return new RunConfigurationManager(
      RunConfigurationProviderFactoryManagerImpl.getInstance().generateProviders( bowlSupplier ) );
  }

  public RunConfigurationManager( List<RunConfigurationProvider> runConfigurationProviders ) {
    this.runConfigurationProviders = runConfigurationProviders;
  }

  private RunConfigurationManager() {
    runConfigurationProviders = RunConfigurationProviderFactoryManagerImpl.getInstance().generateProviders();
  }

  /**
   * Load the RunConfigurations present in each RunConfigurationProvider
   *
   * @return
   */
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
    RunConfigurationProvider runConfigurationProvider =
      runConfiguration != null ? getProvider( runConfiguration.getType() ) : null;
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
    for ( RunConfigurationProvider runConfigurationProvider : this.runConfigurationProviders ) {
      if ( runConfigurationProvider.isSupported( type ) ) {
        runConfigurationProviders.add( runConfigurationProvider );
      }
    }
    return runConfigurationProviders;
  }

  public List<RunConfigurationProvider> getRunConfigurationProviders() {
    List<RunConfigurationProvider> runConfigurationProviders = new ArrayList<>();
    runConfigurationProviders.addAll( this.runConfigurationProviders );
    return runConfigurationProviders;
  }
}
