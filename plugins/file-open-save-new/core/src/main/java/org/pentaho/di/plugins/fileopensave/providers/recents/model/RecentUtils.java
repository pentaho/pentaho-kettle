/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2019 by Hitachi Vantara : http://www.pentaho.com
 *
 *******************************************************************************
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
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
