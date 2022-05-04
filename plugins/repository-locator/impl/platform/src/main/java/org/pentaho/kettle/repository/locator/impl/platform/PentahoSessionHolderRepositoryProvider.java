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
package org.pentaho.kettle.repository.locator.impl.platform;

import org.pentaho.di.core.service.ServiceProvider;
import org.pentaho.di.core.service.ServiceProviderInterface;
import org.pentaho.di.repository.Repository;
import org.pentaho.kettle.repository.locator.api.KettleRepositoryProvider;
import org.pentaho.platform.api.engine.ICacheManager;
import org.pentaho.platform.api.engine.IPentahoSession;
import org.pentaho.platform.engine.core.system.PentahoSessionHolder;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Created by bryan on 3/28/16.
 */
@ServiceProvider( id = "PentahoSessionHolderRepositoryProvider",
  description = "Get the repository from the provided PentahoSession", provides = KettleRepositoryProvider.class )
public class PentahoSessionHolderRepositoryProvider implements KettleRepositoryProvider, ServiceProviderInterface<KettleRepositoryProvider> {
  public static final String REGION = "pdi-repository-cache";
  private static final Logger LOGGER = LoggerFactory.getLogger( PentahoSessionHolderRepositoryProvider.class );
  private final Supplier<IPentahoSession> pentahoSessionSupplier;
  private final Function<IPentahoSession, ICacheManager> cacheManagerFunction;

  public PentahoSessionHolderRepositoryProvider() {
    this( PentahoSessionHolder::getSession, PentahoSystem::getCacheManager );
  }

  public PentahoSessionHolderRepositoryProvider( Supplier<IPentahoSession> pentahoSessionSupplier,
                                                 Function<IPentahoSession, ICacheManager> cacheManagerFunction ) {
    this.pentahoSessionSupplier = pentahoSessionSupplier;
    this.cacheManagerFunction = cacheManagerFunction;
  }

  @Override public Repository getRepository() {
    IPentahoSession session = pentahoSessionSupplier.get();
    if ( session == null ) {
      LOGGER.debug( "No active Pentaho Session, attempting to load PDI repository unauthenticated." );
      return null;
    }
    ICacheManager cacheManager = cacheManagerFunction.apply( session );

    String sessionName = session.getName();
    Repository repository = (Repository) cacheManager.getFromRegionCache( REGION, sessionName );
    if ( repository == null ) {
      LOGGER.debug( "Repository not cached for user: " + sessionName + "." );
      return null;
    }
    return repository;
  }

  @Override
  public int getPriority() {
    return 150;
  }
}
