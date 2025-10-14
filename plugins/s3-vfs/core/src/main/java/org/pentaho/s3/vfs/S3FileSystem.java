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
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemOptions;
import org.apache.commons.vfs2.provider.AbstractFileName;
import org.pentaho.di.core.util.StorageUnitConverter;
import org.pentaho.s3common.S3KettleProperty;
import org.pentaho.s3common.S3CommonFileSystem;

public class S3FileSystem extends S3CommonFileSystem {

  protected S3FileSystem( final FileName rootName, final FileSystemOptions fileSystemOptions ) {
    super( rootName, fileSystemOptions );
  }

  protected S3FileSystem( final FileName rootName, final FileSystemOptions fileSystemOptions,
                          final StorageUnitConverter storageUnitConverter, final S3KettleProperty s3KettleProperty ) {
    super( rootName, fileSystemOptions, storageUnitConverter, s3KettleProperty );
  }

  protected FileObject createFile( AbstractFileName name ) {
    return new S3FileObject( name, this );
  }

}
