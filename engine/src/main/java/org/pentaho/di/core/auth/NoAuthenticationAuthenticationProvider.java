/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 - 2026 by Pentaho Canada Inc. : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2030-06-15
 ******************************************************************************/



package org.pentaho.di.core.auth;

import org.pentaho.di.core.auth.core.AuthenticationProvider;

public class NoAuthenticationAuthenticationProvider implements AuthenticationProvider {
  public static final String NO_AUTH_ID = "NO_AUTH";

  @Override
  public String getDisplayName() {
    return "No Authentication";
  }

  @Override
  public String getId() {
    return NO_AUTH_ID;
  }

}
