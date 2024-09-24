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

package org.pentaho.di.connections.common.domain;

import org.apache.commons.vfs2.Capability;
import org.apache.commons.vfs2.FileName;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystem;
import org.apache.commons.vfs2.FileSystemOptions;
import org.apache.commons.vfs2.provider.AbstractFileName;
import org.apache.commons.vfs2.provider.AbstractFileSystem;

import java.util.Collection;

public class TestFileWithDomainSystem extends AbstractFileSystem implements FileSystem {

  public TestFileWithDomainSystem( FileName rootName, FileSystemOptions fileSystemOptions ) {
    super( rootName, null, fileSystemOptions );
  }

  @Override protected FileObject createFile( AbstractFileName abstractFileName ) throws Exception {
    return new TestFileWithDomainObject( abstractFileName, this );
  }

  @Override protected void addCapabilities( Collection<Capability> collection ) {
    collection.addAll( TestFileWithDomainProvider.capabilities );
  }
}
