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

package org.pentaho.s3n.vfs;

import org.apache.commons.vfs2.FileName;
import org.apache.commons.vfs2.FileType;
import org.apache.commons.vfs2.provider.AbstractFileName;

/**
 * Custom filename that represents an S3 file with the bucket and its relative path
 *
 * @author asimoes
 * @since 09-11-2017
 */
public class S3NFileName extends AbstractFileName {
  public static final String DELIMITER = "/";

  private String bucketId;
  private String bucketRelativePath;
  private String keys;

  public S3NFileName( String scheme, String bucketId, String path, FileType type, String keys ) {
    this( scheme, bucketId, path, type );
    this.keys = keys;
  }

  public S3NFileName( String scheme, String bucketId, String path, FileType type ) {
    super( scheme, path, type );

    this.bucketId = bucketId;

    if ( path.length() > 1 ) {
      this.bucketRelativePath = path.substring( 1 );
      if ( type.equals( FileType.FOLDER ) ) {
        this.bucketRelativePath += DELIMITER;
      }
    } else {
      this.bucketRelativePath = "";
    }
  }

  @Override
  public String getURI() {
    final StringBuilder buffer = new StringBuilder();
    appendRootUri( buffer, false );
    buffer.append( getPath() );
    return buffer.toString();
  }

  public String getBucketId() {
    return bucketId;
  }

  public String getBucketRelativePath() {
    return bucketRelativePath;
  }

  @Override
  public FileName createName( String absPath, FileType type ) {
    return new S3NFileName( getScheme(), bucketId, absPath, type );
  }

  @Override
  protected void appendRootUri( StringBuilder buffer, boolean addPassword ) {
    buffer.append( getScheme() );
    buffer.append( ":/" );
    if ( keys != null ) {
      buffer.append('/').append( bucketId );
    }
  }
}
