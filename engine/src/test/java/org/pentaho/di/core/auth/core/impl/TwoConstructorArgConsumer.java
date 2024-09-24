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

package org.pentaho.di.core.auth.core.impl;

import org.pentaho.di.core.auth.core.AuthenticationConsumer;
import org.pentaho.di.core.auth.core.AuthenticationConsumptionException;

public class TwoConstructorArgConsumer implements AuthenticationConsumer<Object, Object> {
  public TwoConstructorArgConsumer( String one, Integer two ) {

  }

  @Override
  public Object consume( Object authenticationProvider ) throws AuthenticationConsumptionException {
    return null;
  }

}
