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

import org.pentaho.di.engine.configuration.api.RunConfigurationProvider;
import org.pentaho.di.engine.configuration.impl.pentaho.DefaultRunConfigurationProviderFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * The RunConfigurationProviderFactoryManager is used to manage the registration of RunConfiguration types and
 * generate their RunConfigurationProviders for RunConfigurationManager instances associated with Projects
 * <p>
 * Each RunConfiguration type (e.g. Pentaho, Spark) has a RunConfigurationProvider that allows users to create
 * RunConfigurations of that type.
 * Since RunConfigurationProviders are tied to metastores, and metastores are tied to Projects, we need to be able to
 * create (via Factories) instances of these Providers for each RunConfigurationManager instance associated with each
 * Project.
 */
public class RunConfigurationProviderFactoryManager {
  private static RunConfigurationProviderFactoryManager instance;

  private List<RunConfigurationProviderFactory> factories;

  public static RunConfigurationProviderFactoryManager getInstance() {
    if ( null == instance ) {
      instance = new RunConfigurationProviderFactoryManager();
    }
    return instance;
  }

  public RunConfigurationProviderFactoryManager() {
    factories = new ArrayList<>();
    factories.add( new DefaultRunConfigurationProviderFactory() );
  }

  /**
   * Register a RunConfigurationProviderFactory with the RunConfigurationProviderFactoryManager so that its type will
   * be generated in {@link #generateProviders(CheckedMetaStoreSupplier)}
   *
   * @param factory
   */
  public void registerFactory( RunConfigurationProviderFactory factory ) {
    factories.add( factory );
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
}
