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


package org.pentaho.di.core.auth;

import org.pentaho.di.core.auth.core.AuthenticationConsumer;

/**
 * The AuthenticationProvider interface specifies the operations needed to interact with an authentication method.
 */
public interface AuthenticationConsumerType {
  public String getDisplayName();

  public Class<? extends AuthenticationConsumer<?, ?>> getConsumerClass();
}
