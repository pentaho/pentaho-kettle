/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2017-2023 by Hitachi Vantara : http://www.pentaho.com
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
