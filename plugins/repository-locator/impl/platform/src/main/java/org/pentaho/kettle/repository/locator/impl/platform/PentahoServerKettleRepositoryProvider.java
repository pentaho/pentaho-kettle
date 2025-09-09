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

package org.pentaho.kettle.repository.locator.impl.platform;

import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.service.ServiceProvider;
import org.pentaho.di.core.service.ServiceProviderInterface;
import org.pentaho.di.repository.Repository;
import org.pentaho.di.www.CarteSingleton;
import org.pentaho.di.www.SlaveServerConfig;
import org.pentaho.di.www.TransformationMap;
import org.pentaho.kettle.repository.locator.api.KettleRepositoryProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by bryan on 3/28/16.
 */
@ServiceProvider( id = "PentahoServerKettleRepositoryProvider",
  description = "Get the repository from the provided Carte Server", provides = KettleRepositoryProvider.class )
public class PentahoServerKettleRepositoryProvider implements KettleRepositoryProvider, ServiceProviderInterface<KettleRepositoryProvider> {
  private static final Logger LOGGER = LoggerFactory.getLogger( PentahoServerKettleRepositoryProvider.class );

  public PentahoServerKettleRepositoryProvider() {
  }

  @Override public Repository getRepository() {
      TransformationMap transformationMap = CarteSingleton.getInstance().getTransformationMap();
      SlaveServerConfig slaveServerConfig = transformationMap.getSlaveServerConfig();
      Repository repository = null;
      try {
          repository = slaveServerConfig.getRepository();
      } catch (KettleException e) {
        LOGGER.error( "Unable to get repository from slave server config" );
      }
      return repository;
  }

  @Override
  public int getPriority() {
    return 150;
  }
}
