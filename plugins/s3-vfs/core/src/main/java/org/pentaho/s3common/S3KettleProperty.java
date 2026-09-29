/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 - 2026 by Pentaho Canada Inc. : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2030-06-15
 ******************************************************************************/



package org.pentaho.s3common;

import org.pentaho.di.core.Const;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.util.EnvUtil;
import org.pentaho.di.i18n.BaseMessages;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Properties;

/**
 * Class that handles operations dealing with kettle property file.
 */
public class S3KettleProperty {
  private static final Class<?> PKG = S3KettleProperty.class;
  private static final Logger logger = LoggerFactory.getLogger( S3KettleProperty.class );

  public static final String S3VFS_PART_SIZE = "s3.vfs.partSize";

  // Controls the TTL (in seconds) of the in-memory VFS caches added to speed up S3 file-listing
  // operations (listing-metadata reuse on attach, and bucket-existence checks). Only applies while the
  // cache is enabled (see S3VFS_CACHE_ENABLED below); tune this to trade off staleness tolerance vs.
  // fewer S3 calls. Defaults to 60 seconds when unset/invalid.
  public static final String S3VFS_CACHE_TTL_SECONDS = "s3.vfs.cache.ttlSeconds";
  public static final int S3VFS_CACHE_TTL_SECONDS_DEFAULT = 60;

  // Explicit on/off switch for the S3 VFS listing/bucket-existence caches described above. Defaults to
  // enabled (true). Set to "false" for deployments where external processes may create/delete/modify
  // the same buckets or objects concurrently and cannot tolerate any staleness.
  public static final String S3VFS_CACHE_ENABLED = "s3.vfs.cache.enabled";
  public static final boolean S3VFS_CACHE_ENABLED_DEFAULT = true;

  public String getPartSize() {
    return getProperty( S3VFS_PART_SIZE );
  }

  /**
   * @return whether the S3 VFS listing/bucket-existence caches are enabled. Defaults to {@code true}
   * ({@link #S3VFS_CACHE_ENABLED_DEFAULT}) when the property is unset or blank. Any value other than
   * a case-insensitive "false" is treated as enabled.
   */
  public boolean isCacheEnabled() {
    String value = getProperty( S3VFS_CACHE_ENABLED );

    if ( value == null || value.trim().isEmpty() ) {
      return S3VFS_CACHE_ENABLED_DEFAULT;
    }

    return !Boolean.FALSE.toString().equalsIgnoreCase( value.trim() );
  }

  /**
   * @return the configured TTL, in seconds, for the S3 VFS listing/bucket-existence caches (only
   * meaningful while {@link #isCacheEnabled()} is {@code true}). Falls back to
   * {@link #S3VFS_CACHE_TTL_SECONDS_DEFAULT} when the property is unset, blank, zero/negative, or not a
   * valid integer.
   */
  public int getCacheTtlSeconds() {
    String value = getProperty( S3VFS_CACHE_TTL_SECONDS );

    if ( value == null || value.trim().isEmpty() ) {
      return S3VFS_CACHE_TTL_SECONDS_DEFAULT;
    }

    try {
      int ttlSeconds = Integer.parseInt( value.trim() );

      if ( ttlSeconds <= 0 ) {
        logger.warn( "Ignoring non-positive value '{}' for {}; using default of {}s. To disable the "
            + "cache entirely, use {}=false instead.", value, S3VFS_CACHE_TTL_SECONDS,
          S3VFS_CACHE_TTL_SECONDS_DEFAULT, S3VFS_CACHE_ENABLED );
        return S3VFS_CACHE_TTL_SECONDS_DEFAULT;
      }

      return ttlSeconds;
    } catch ( NumberFormatException e ) {
      logger.warn( "Ignoring invalid value '{}' for {}; using default of {}s", value,
        S3VFS_CACHE_TTL_SECONDS, S3VFS_CACHE_TTL_SECONDS_DEFAULT );
      return S3VFS_CACHE_TTL_SECONDS_DEFAULT;
    }
  }

  public String getProperty( String property ) {
    String filename =  Const.getKettlePropertiesFilename();
    Properties properties;
    String partSizeString = "";
    try {
      properties = EnvUtil.readProperties( filename );
      partSizeString = properties.getProperty( property );
    } catch ( KettleException ke ) {
      logger.error( BaseMessages.getString( PKG, "WARN.S3Common.PropertyNotFound",
        property, filename ) );
    }
    return partSizeString;
  }
}
