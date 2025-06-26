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

import com.google.common.annotations.VisibleForTesting;
import org.pentaho.di.core.exception.KettlePluginException;
import org.pentaho.di.core.service.PluginServiceLoader;
import org.pentaho.di.engine.configuration.api.CheckedMetaStoreSupplier;
import org.pentaho.di.engine.configuration.api.RunConfigurationProvider;
import org.pentaho.di.engine.configuration.api.RunConfigurationProviderFactory;
import org.pentaho.di.engine.configuration.api.RunConfigurationProviderFactoryManager;
import org.pentaho.di.engine.configuration.impl.pentaho.DefaultRunConfigurationProviderFactory;
import org.pentaho.di.metastore.MetaStoreConst;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * The RunConfigurationProviderFactoryManager is used to manage the registration of RunConfiguration types and
 * generate their RunConfigurationProviders for RunConfigurationManager instances associated with Projects
 * <p>
 * Each RunConfiguration type has a RunConfigurationProvider that allows users to create
 * RunConfigurations of that type.
 * Since RunConfigurationProviders are tied to metastores, and metastores are tied to Projects, we need to be able to
 * create (via Factories) instances of these Providers for each RunConfigurationManager instance associated with each
 * Project.
 */
public class RunConfigurationProviderFactoryManagerImpl implements RunConfigurationProviderFactoryManager {
  private static RunConfigurationProviderFactoryManagerImpl instance;

  private ArrayList<RunConfigurationProviderFactory> factories;

  private static Logger logger = LoggerFactory.getLogger( RunConfigurationProviderFactoryManagerImpl.class );

  public static synchronized RunConfigurationProviderFactoryManagerImpl getInstance() {
    if ( null == instance ) {
      instance = new RunConfigurationProviderFactoryManagerImpl();
    }
    return instance;
  }

  private RunConfigurationProviderFactoryManagerImpl() {
    factories = new ArrayList<>();
    factories.add( new DefaultRunConfigurationProviderFactory() );

    try {
      factories.addAll( PluginServiceLoader.loadServices( RunConfigurationProviderFactory.class ) );
    } catch ( KettlePluginException e ) {
      logger.warn( "Error loading plugin RunConfigurationProviderFactory(s)", e );
    }
  }

  public List<RunConfigurationProvider> generateProviders() {
    return generateProviders( MetaStoreConst::getDefaultMetastore );
  }

  /**
   * Generates (instantiates) a RunConfigurationProvider of each registered Factory type using the provided
   * CheckedMetaStoreSupplier
   *
   * @param checkedMetaStoreSupplier
   * @return List<RunConfigurationProvider>
   */
  public List<RunConfigurationProvider> generateProviders( CheckedMetaStoreSupplier checkedMetaStoreSupplier ) {
    List<RunConfigurationProvider> providers = new ArrayList<>();
    factories.forEach( factory -> providers.add( factory.getProvider( checkedMetaStoreSupplier ) ) );
    return providers;
  }

  /**
   * Used by test class
   */
  @VisibleForTesting
  void setFactories( ArrayList<RunConfigurationProviderFactory> factories ) {
    this.factories = factories;
  }

  @VisibleForTesting
  ArrayList<RunConfigurationProviderFactory> getFactories() {
    return factories;
  }
}
