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



package org.pentaho.di.version;

public class EnvironmentVariableGetter {
  public String getEnvVarible( String name ) throws Exception {
    String result = System.getenv( name );
    if ( result == null ) {
      throw new RuntimeException( name + " undefined" );
    }
    return result;
  }
}
