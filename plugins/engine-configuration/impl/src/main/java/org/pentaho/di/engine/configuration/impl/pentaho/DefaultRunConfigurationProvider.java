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


package org.pentaho.di.engine.configuration.impl.pentaho;

import org.pentaho.di.core.util.Utils;
import org.pentaho.di.engine.configuration.api.RunConfiguration;
import org.pentaho.di.engine.configuration.api.RunConfigurationExecutor;
import org.pentaho.di.engine.configuration.api.RunConfigurationProvider;
import org.pentaho.di.engine.configuration.api.CheckedMetaStoreSupplier;
import org.pentaho.di.engine.configuration.api.MetaStoreRunConfigurationFactory;
import org.pentaho.di.job.JobMeta;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.metastore.api.exceptions.MetaStoreException;
import org.pentaho.metastore.persist.MetaStoreFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Created by bmorrise on 3/16/17.
 */
public class DefaultRunConfigurationProvider extends MetaStoreRunConfigurationFactory
  implements RunConfigurationProvider {

  public static final String DEFAULT_CONFIG_NAME = "Pentaho local";
  private static final String TYPE = "Pentaho";
  private List<String> supported = Arrays.asList( TransMeta.XML_TAG, JobMeta.XML_TAG );

  private static DefaultRunConfiguration defaultRunConfiguration = new DefaultRunConfiguration();

  private DefaultRunConfigurationExecutor defaultRunConfigurationExecutor;

  static {
    defaultRunConfiguration.setName( DEFAULT_CONFIG_NAME );
    defaultRunConfiguration.setReadOnly( true );
    defaultRunConfiguration.setLocal( true );
  }

  public DefaultRunConfigurationProvider( CheckedMetaStoreSupplier metastoreSupplier ) {
    super( metastoreSupplier );
    this.defaultRunConfigurationExecutor = DefaultRunConfigurationExecutor.getInstance();
  }

  @Override public RunConfiguration getConfiguration() {
    return new DefaultRunConfiguration();
  }

  @Override public String getType() {
    return TYPE;
  }

  @SuppressWarnings( "unchecked" )
  protected MetaStoreFactory<DefaultRunConfiguration> getMetaStoreFactory() throws MetaStoreException {
    return getMetastoreFactory( DefaultRunConfiguration.class );
  }

  @Override public List<RunConfiguration> load() {
    List<RunConfiguration> runConfigurations = new ArrayList<>();
    runConfigurations.add( defaultRunConfiguration );
    runConfigurations.addAll( super.load() );
    return runConfigurations;
  }

  @Override public RunConfiguration load( String name ) {
    if ( Utils.isEmpty( name ) || name.equals( DEFAULT_CONFIG_NAME ) ) {
      return defaultRunConfiguration;
    }
    return super.load( name );
  }

  @Override public List<String> getNames() {
    List<String> names = new ArrayList<>();
    names.add( defaultRunConfiguration.getName() );
    names.addAll( super.getNames() );
    return names;
  }

  @Override public boolean isSupported( String type ) {
    return supported.contains( type );
  }

  @Override public List<String> getNames( String type ) {
    return isSupported( type ) ? getNames() : Collections.emptyList();
  }

  @Override public RunConfigurationExecutor getExecutor() {
    return defaultRunConfigurationExecutor;
  }
}
