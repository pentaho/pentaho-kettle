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


package org.pentaho.s3a.vfs;

import org.apache.commons.vfs2.FileName;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemOptions;
import org.apache.commons.vfs2.provider.AbstractFileName;
import org.pentaho.s3common.S3CommonFileSystem;

public class S3AFileSystem extends S3CommonFileSystem {

  protected S3AFileSystem( final FileName rootName, final FileSystemOptions fileSystemOptions ) {
    super( rootName, fileSystemOptions );
  }

  protected FileObject createFile( AbstractFileName name ) throws Exception {
    return new S3AFileObject( name, this );
  }

}
