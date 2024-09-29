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


package org.pentaho.di.job.entries;

public class FTPUtils {
  public static String FILE_SEPARATOR = "/";

  /**
   * normalize / to \ and remove trailing slashes from a path
   *
   * @param path
   * @return normalized path
   * @throws Exception
   */
  public static String normalizePath( String path ) throws Exception {
    if ( path == null ) {
      return path;
    }
    String normalizedPath = path.replaceAll( "\\\\", FILE_SEPARATOR );
    while ( normalizedPath.endsWith( "\\" ) || normalizedPath.endsWith( FILE_SEPARATOR ) ) {
      normalizedPath = normalizedPath.substring( 0, normalizedPath.length() - 1 );
    }

    return normalizedPath;
  }
}
