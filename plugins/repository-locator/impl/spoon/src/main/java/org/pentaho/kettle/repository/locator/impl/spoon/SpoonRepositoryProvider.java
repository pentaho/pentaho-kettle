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

package org.pentaho.kettle.repository.locator.impl.spoon;

import org.pentaho.di.core.service.ServiceProvider;
import org.pentaho.di.core.service.ServiceProviderInterface;
import org.pentaho.di.repository.Repository;
import org.pentaho.di.ui.spoon.Spoon;
import org.pentaho.kettle.repository.locator.api.KettleRepositoryProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.function.Supplier;

/**
 * Created by bryan on 4/15/16.
 */
@ServiceProvider( id = "SpoonRepositoryProviderProvider",
  description = "Get the repository from the provided PentahoSession", provides = KettleRepositoryProvider.class )
public class SpoonRepositoryProvider implements KettleRepositoryProvider, ServiceProviderInterface<KettleRepositoryProvider> {
  private static Logger log = LoggerFactory.getLogger( SpoonRepositoryProvider.class.getName() );
  private final Supplier<Spoon> spoonSupplier;

  public SpoonRepositoryProvider() {
    this( Spoon::getInstance );
  }

  public SpoonRepositoryProvider( Supplier<Spoon> spoonSupplier ) {
    this.spoonSupplier = spoonSupplier;
  }

  @Override public Repository getRepository() {
    try {
      return spoonSupplier.get().getRepository();
    } catch ( Exception e ) {
      log.warn( e.getMessage() );
      return null;
    }
  }

  @Override
  public int getPriority() {
    return 150;
  }
}
