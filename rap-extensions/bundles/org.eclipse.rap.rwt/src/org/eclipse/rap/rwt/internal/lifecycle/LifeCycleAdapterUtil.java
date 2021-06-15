/*******************************************************************************
 * Copyright (c) 2008, 2015 Innoopract Informationssysteme GmbH and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Innoopract Informationssysteme GmbH - initial API and implementation
 *    EclipseSource - ongoing development
 ******************************************************************************/
package org.eclipse.rap.rwt.internal.lifecycle;

import java.util.Locale;


public final class LifeCycleAdapterUtil {

  private LifeCycleAdapterUtil() {
    // prevent instatiation
  }

  public static String[] getKitPackageVariants( String className ) {
    String simpleClassName = getSimpleClassName( className ).toLowerCase( Locale.ENGLISH );
    String[] pkgSegments = getPackageName( className ).split( "\\." );
    String[] result = new String[ pkgSegments.length ];
    for( int i = 0; i < result.length; i++ ) {
      result[ i ] = createInternalPackage( pkgSegments, simpleClassName, i );
    }
    return result;
  }

  public static String getSimpleClassName( String className ) {
    int index = className.lastIndexOf( '.' );
    return index == -1 ? className : className.substring( index + 1 );
  }

  public static String getPackageName( String className ) {
    int index = className.lastIndexOf( '.' );
    return index == -1 ? "" : className.substring( 0, index );
  }

  private static String createInternalPackage( String[] pkgSegments, String className, int index ) {
    StringBuilder buffer = new StringBuilder();
    for( int i = 0; i < pkgSegments.length; i++ ) {
      buffer.append( pkgSegments[i] );
      buffer.append( '.' );
      if( i == index ) {
        buffer.append( "internal." );
      }
    }
    buffer.append( className );
    buffer.append( "kit" );
    return buffer.toString();
  }

}
