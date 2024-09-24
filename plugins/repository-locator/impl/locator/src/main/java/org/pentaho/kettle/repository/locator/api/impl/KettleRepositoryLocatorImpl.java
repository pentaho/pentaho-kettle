/*!
 * Copyright 2010 - 2022 Hitachi Vantara.  All rights reserved.
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
package org.pentaho.kettle.repository.locator.api.impl;

import org.pentaho.di.core.exception.KettlePluginException;
import org.pentaho.di.core.service.PluginServiceLoader;
import org.pentaho.di.core.service.ServiceProvider;
import org.pentaho.di.core.service.ServiceProviderInterface;
import org.pentaho.di.repository.Repository;
import org.pentaho.kettle.repository.locator.api.KettleRepositoryLocator;
import org.pentaho.kettle.repository.locator.api.KettleRepositoryProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.Objects;

/**
 * Created by bryan on 4/15/16.
 */
@ServiceProvider( id = "KettleRepositoryLocator", description = "Provides access to the repository", provides = KettleRepositoryLocator.class )
public class KettleRepositoryLocatorImpl implements KettleRepositoryLocator, ServiceProviderInterface<KettleRepositoryLocator> {

  Logger logger = LoggerFactory.getLogger( KettleRepositoryLocator.class );

  @Override public Repository getRepository() {
    try {
      // NOTE: this class formerly used a ranking system to prioritize the providers registered and would check them in order
      // of priority for the first non-null repository.  In practice, we only ever registered one at a time, spoon or PUC.
      // As such, the priority ranking is gone and will need to be reintroduced if desired later.
      Collection<KettleRepositoryProvider> repositoryProviders = PluginServiceLoader.loadServices( KettleRepositoryProvider.class );
      return repositoryProviders.stream().map( KettleRepositoryProvider::getRepository ).filter( Objects::nonNull ).findFirst().orElse( null );
    } catch ( KettlePluginException e ) {
      logger.error( "Error getting repository", e );
    }
    return null;
  }
}
