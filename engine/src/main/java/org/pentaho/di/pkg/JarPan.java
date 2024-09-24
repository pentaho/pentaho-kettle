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

package org.pentaho.di.pkg;

import org.pentaho.di.pan.Pan;

/**
 * Executes a transformation calls transformation.xml from within a jar file.
 *
 * @author Matt
 * @since
 */
public class JarPan {
  public static void main( String[] a ) {
    String[] args = new String[a.length + 1];
    args[0] = "-jarfile:/" + JarfileGenerator.TRANSFORMATION_FILENAME;
    for ( int i = 0; i < a.length; i++ ) {
      args[i + 1] = a[i];
    }

    try {
      Pan.main( args );
    } catch ( Exception ke ) {
      System.out.println( "ERROR occurred: " + ke.getMessage() );
      System.exit( 2 );
    }

  }
}
