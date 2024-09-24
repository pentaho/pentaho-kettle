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

package org.pentaho.di.plugins.fileopensave.providers.recents.model;

import org.pentaho.di.core.LastUsedFile;
import org.pentaho.di.core.util.Utils;

public class RecentUtils {

  public static final char DELIMITER = '/';
  public static final char WINDOWS_DELIMITER = '\\';

  public static boolean isUrl( String path ) {
    return path.matches( "^[\\w]+://.*" );
  }

  public static boolean isWindows( String path ) {
    return path.matches( "^[A-Za-z]+:\\\\.*" );
  }

  private RecentUtils() {
    // This class cannot be instantiated
  }

  public static String getFilename( String path ) {
    if ( isWindows( path ) ) {
      int index = path.lastIndexOf( WINDOWS_DELIMITER );
      return path.substring( index + 1 );
    } else {
      int index = path.lastIndexOf( DELIMITER );
      return path.substring( index + 1 );
    }
  }

  public static String getParent( String path ) {
    if ( isWindows( path ) ) {
      int index = path.lastIndexOf( WINDOWS_DELIMITER );
      return path.substring( 0, index );
    } else {
      int index = path.lastIndexOf( DELIMITER );
      if ( index != -1 ) {
        return path.substring( 0, index );
      }
    }
    return path;
  }

  public static void setPaths( LastUsedFile lastUsedFile, RecentFile recentFile ) {
    if ( !Utils.isEmpty( lastUsedFile.getRepositoryName() ) ) {
      recentFile.setName( lastUsedFile.getFilename() );
      recentFile.setParent( lastUsedFile.getDirectory() );
      recentFile.setPath( lastUsedFile.getDirectory() + DELIMITER + lastUsedFile.getFilename() );
      recentFile.setRepository( lastUsedFile.getRepositoryName() );
      recentFile.setUsername( lastUsedFile.getUsername() );
    } else {
      String path = lastUsedFile.getFilename();
      recentFile.setName( getFilename( path ) );
      recentFile.setParent( getParent( path ) );
      recentFile.setPath( path );
    }
  }

}
