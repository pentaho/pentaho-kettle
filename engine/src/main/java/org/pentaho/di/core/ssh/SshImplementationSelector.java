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

package org.pentaho.di.core.ssh;

import org.pentaho.di.core.Props;

public class SshImplementationSelector {
  private SshImplementationSelector() { }

  public static final String PROP_KEY = "kettle.ssh.impl"; // value: mina

  public static SshImplementation resolve() {
    String v = System.getProperty( PROP_KEY );
    if ( v == null ) {
      try {
        if ( Props.isInitialized() ) {
          v = Props.getInstance().getProperty( PROP_KEY );
        }
      } catch ( Exception ignored ) {
      }
    }
    if ( v == null ) {
      return SshImplementation.MINA;
    }
    v = v.trim().toLowerCase();
    if ( "mina".equals( v ) ) {
      return SshImplementation.MINA;
    }
    return SshImplementation.MINA;
  }
}
