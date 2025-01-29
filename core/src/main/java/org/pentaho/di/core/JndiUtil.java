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


package org.pentaho.di.core;

import java.io.File;

import org.pentaho.di.core.exception.KettleException;

public class JndiUtil {

  public static void initJNDI() throws KettleException {
    String path = Const.JNDI_DIRECTORY;

    if ( path == null || path.equals( "" ) ) {
      try {
        File file = new File( "simple-jndi" );
        path = file.getCanonicalPath();
      } catch ( Exception e ) {
        throw new KettleException( "Error initializing JNDI", e );
      }
      Const.JNDI_DIRECTORY = path;
    }

    System.setProperty( "java.naming.factory.initial", "org.osjava.sj.SimpleContextFactory" );
    System.setProperty( "org.osjava.sj.root", path );
    System.setProperty( "org.osjava.sj.delimiter", "/" );
  }

}
