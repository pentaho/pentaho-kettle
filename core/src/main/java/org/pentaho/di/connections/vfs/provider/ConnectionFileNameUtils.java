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


package org.pentaho.di.connections.vfs.provider;

import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;
import org.apache.commons.lang.StringUtils;

import static org.apache.commons.vfs2.FileName.SEPARATOR;
import static org.apache.commons.vfs2.FileName.SEPARATOR_CHAR;

public class ConnectionFileNameUtils {
  @Nullable
  private static ConnectionFileNameUtils instance;

  @NonNull
  public static ConnectionFileNameUtils getInstance() {
    if ( instance == null ) {
      instance = new ConnectionFileNameUtils();
    }

    return instance;
  }

  protected ConnectionFileNameUtils() {
  }

  public void appendPath( @NonNull StringBuilder urlBuilder, @Nullable String path ) {
    if ( StringUtils.isEmpty( path ) ) {
      return;
    }

    path = trimLeadingSeparator( path );
    if ( StringUtils.isEmpty( path ) ) {
      return;
    }

    ensureTrailingSeparator( urlBuilder );

    urlBuilder.append( path );
  }

  public boolean isDescendantOrSelf( @NonNull String descendantPath, @NonNull String basePath ) {
    return ensureTrailingSeparator( descendantPath ).startsWith( ensureTrailingSeparator( basePath ) );
  }

  @NonNull
  public String trimLeadingSeparator( @NonNull String path ) {
    return path.startsWith( SEPARATOR )
      ? path.substring( 1 )
      : path;
  }

  public void trimLeadingSeparator( @NonNull StringBuilder pathBuilder ) {
    if ( pathBuilder.length() > 0 && pathBuilder.charAt( 0 ) == SEPARATOR_CHAR ) {
      pathBuilder.delete( 0, 1 );
    }
  }

  @NonNull
  public String ensureLeadingSeparator( @NonNull String path ) {
    return !path.startsWith( SEPARATOR ) ? ( SEPARATOR + path ) : path;
  }

  public void ensureLeadingSeparator( @NonNull StringBuilder pathBuilder ) {
    if ( pathBuilder.length() == 0 || pathBuilder.charAt( 0 ) != SEPARATOR_CHAR ) {
      pathBuilder.insert( 0, SEPARATOR_CHAR );
    }
  }

  @NonNull
  public String ensureTrailingSeparator( @NonNull String path ) {
    return !path.endsWith( SEPARATOR ) ? ( path + SEPARATOR ) : path;
  }

  public void ensureTrailingSeparator( @NonNull StringBuilder pathBuilder ) {
    int length = pathBuilder.length();
    if ( length > 0 && pathBuilder.charAt( length - 1 ) != SEPARATOR_CHAR ) {
      pathBuilder.append( SEPARATOR_CHAR );
    }
  }
}
