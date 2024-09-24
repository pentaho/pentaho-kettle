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

package org.pentaho.di.connections.common.bucket;

import org.apache.commons.vfs2.Capability;
import org.apache.commons.vfs2.FileName;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystem;
import org.apache.commons.vfs2.FileSystemOptions;
import org.apache.commons.vfs2.provider.AbstractFileName;
import org.apache.commons.vfs2.provider.AbstractFileSystem;

import java.util.Collection;

public class TestFileWithBucketsSystem extends AbstractFileSystem implements FileSystem {

  public TestFileWithBucketsSystem( FileName rootName, FileSystemOptions fileSystemOptions ) {
    super( rootName, null, fileSystemOptions );
  }

  @Override protected FileObject createFile( AbstractFileName abstractFileName ) throws Exception {
    return new TestFileWithBucketsObject( abstractFileName, this );
  }

  @Override protected void addCapabilities( Collection<Capability> collection ) {
    collection.addAll( TestFileWithBucketsProvider.capabilities );
  }
}
