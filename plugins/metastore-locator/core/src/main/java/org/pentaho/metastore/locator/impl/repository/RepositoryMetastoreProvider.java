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
package org.pentaho.metastore.locator.impl.repository;

import org.pentaho.di.core.exception.KettlePluginException;
import org.pentaho.di.core.service.PluginServiceLoader;
import org.pentaho.di.core.service.ServiceProvider;
import org.pentaho.di.core.service.ServiceProviderInterface;
import org.pentaho.di.repository.Repository;
import org.pentaho.kettle.repository.locator.api.KettleRepositoryLocator;
import org.pentaho.metastore.api.IMetaStore;
import org.pentaho.metastore.locator.api.MetastoreLocator;
import org.pentaho.metastore.locator.api.MetastoreProvider;

import java.util.Collection;
import java.util.Optional;

/**
 * Created by bryan on 3/29/16.
 */
@ServiceProvider( id = "RepositoryMetastoreProvider", description = "Provides access to a local file metastore", provides = MetastoreProvider.class )
public class RepositoryMetastoreProvider implements MetastoreProvider, ServiceProviderInterface<MetastoreProvider> {
  private KettleRepositoryLocator kettleRepositoryLocator;

  public RepositoryMetastoreProvider( KettleRepositoryLocator kettleRepositoryLocator ) {
    this.kettleRepositoryLocator = kettleRepositoryLocator;
  }

  public RepositoryMetastoreProvider() {
    try {
      Collection<KettleRepositoryLocator> repositoryLocators = PluginServiceLoader.loadServices( KettleRepositoryLocator.class );
      Optional<KettleRepositoryLocator> kettleRepositoryLocatorOptional = repositoryLocators.stream().findFirst();
      if ( kettleRepositoryLocatorOptional.isPresent() )
        kettleRepositoryLocator = repositoryLocators.stream().findFirst().orElse(null );
    } catch ( KettlePluginException e ) {
      e.printStackTrace();
    }
  }

  @Override public IMetaStore getMetastore() {
    if ( kettleRepositoryLocator != null ) {
      Repository repository = kettleRepositoryLocator.getRepository();
      if ( repository != null ) {
        return repository.getRepositoryMetaStore();
      }
    }
    return null;
  }

  @Override
  public String getProviderType() {
    return MetastoreLocator.REPOSITORY_PROVIDER_KEY;
  }
}
