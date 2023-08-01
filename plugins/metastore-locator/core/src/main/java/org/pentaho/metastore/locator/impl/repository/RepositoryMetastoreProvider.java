/*!
 * Copyright 2010 - 2023 Hitachi Vantara.  All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */
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
