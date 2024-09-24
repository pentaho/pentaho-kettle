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

package org.pentaho.di.plugins.fileopensave.util;

import java.util.regex.Pattern;

/**
 * Created by bmorrise on 8/16/17.
 */
public class Util {

  public static boolean isFiltered( String value, String filter ) {
    filter = filter.replace( ".", "\\." );
    filter = filter.replace( "*", ".*" );

    return !Pattern.matches( filter, value );
  }

  public static String getName( String path ) {
    //Strips off extension if present
    int slashpos = path.lastIndexOf( "/" );
    int dotpos = path.lastIndexOf( "." );
    if ( slashpos < dotpos ) {
      return path.substring( slashpos + 1, dotpos );
    } else {
      return path.substring( slashpos + 1 );
    }
  }

  public static String getFolder( String path ) {
    if ( path.endsWith( ":\\\\" ) || path.endsWith( "://" ) ) {
      return null; //no parent of root
    }
    if ( path.lastIndexOf( "/" ) == -1 ) {
      if ( path.lastIndexOf( "\\" ) == -1 ) {
        return null; //No parent if no slashes
      } else {
        return path.substring( 0, path.lastIndexOf( "\\" ) );
      }
    }
    return path.substring( 0, path.lastIndexOf( "/" ) );
  }
}
