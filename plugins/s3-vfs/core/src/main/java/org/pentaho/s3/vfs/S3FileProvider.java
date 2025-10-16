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


package org.pentaho.s3.vfs;

import org.apache.commons.vfs2.FileName;
import org.apache.commons.vfs2.FileSystem;
import org.apache.commons.vfs2.FileSystemOptions;
import org.pentaho.s3common.S3CommonFileProvider;

public class S3FileProvider extends S3CommonFileProvider {

  /**
   * The scheme this provider was designed to support.
   * <p> Copied to </p>
   * @module legacy-amazon
   * @interface S3Client.java
   */
  public static final String SCHEME = "s3";

  /**
   * User Information.
   */
  public static final String ATTR_USER_INFO = "UI";

  public S3FileProvider() {
    super();
    setFileNameParser( S3FileNameParser.getInstance() );
  }

  protected FileSystem doCreateFileSystem( final FileName name, final FileSystemOptions fileSystemOptions ) {
    return new S3FileSystem( name, fileSystemOptions );
  }

}
