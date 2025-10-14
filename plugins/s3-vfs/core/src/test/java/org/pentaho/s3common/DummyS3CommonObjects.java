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


package org.pentaho.s3common;

import org.apache.commons.vfs2.FileName;
import org.apache.commons.vfs2.FileSystemOptions;
import org.apache.commons.vfs2.FileType;
import org.apache.commons.vfs2.provider.AbstractFileName;
import org.pentaho.di.core.util.StorageUnitConverter;

public class DummyS3CommonObjects {

  protected static class DummyS3FileName extends AbstractFileName {

    private final String bucketId;

    protected DummyS3FileName( String scheme, String bucketId, String path, FileType type ) {
      super( scheme, path, type );
      this.bucketId = bucketId;
    }

    @Override
    protected void appendRootUri( StringBuilder buffer, boolean addPassword ) {
      buffer.append( getScheme() ).append( "://" ).append( bucketId ).append( "/" );
    }

    @Override
    public String getURI() {
      return getScheme() + "://" + bucketId + "/" + getPath();
    }

    @Override
    public FileName createName( String absPath, FileType type ) {
      return new DummyS3FileName( getScheme(), bucketId, absPath, type );
    }
  }

  protected static class DummyS3FileObject extends S3CommonFileObject {
    protected DummyS3FileObject( final DummyS3FileName name, final S3CommonFileSystem fileSystem ) {
      super( name, fileSystem );
    }
  }

  protected static class DummyS3FileSystem extends S3CommonFileSystem {

    protected DummyS3FileSystem( final FileName rootName, final FileSystemOptions fileSystemOptions ) {
      super( rootName, fileSystemOptions );
    }

    protected DummyS3FileSystem( final FileName rootName, final FileSystemOptions fileSystemOptions,
                                 final StorageUnitConverter storageUnitConverter, final S3KettleProperty s3KettleProperty ) {
      super( rootName, fileSystemOptions, storageUnitConverter, s3KettleProperty );
    }

    @Override
    protected DummyS3FileObject createFile( AbstractFileName name ) {
      return new DummyS3FileObject( (DummyS3FileName) name, this );
    }
  }

  protected static DummyS3FileSystem getDummyInstance() {
    // Use a real S3FileName with dummy but non-null values to avoid NPE in S3Util.getKeysFromURI
    DummyS3FileName rootName = new DummyS3FileName( "s3", "bucket", "/bucket/key", FileType.FOLDER );
    FileSystemOptions fileSystemOptions = new FileSystemOptions();
    return new DummyS3FileSystem( rootName, fileSystemOptions );
  }

}
