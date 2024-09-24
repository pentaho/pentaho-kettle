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

package org.pentaho.googledrive.vfs.test;

import org.apache.commons.vfs2.Capability;
import org.apache.commons.vfs2.FileName;
import org.apache.commons.vfs2.FileSystemOptions;
import org.junit.Test;
import org.pentaho.googledrive.vfs.GoogleDriveFileSystem;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;

public class GoogleDriveFileSystemTest {

  protected static final Collection<Capability>
      capabilities =
      Collections.unmodifiableCollection( Arrays.asList(
          new Capability[] { Capability.CREATE, Capability.DELETE, Capability.RENAME, Capability.GET_TYPE,
              Capability.LIST_CHILDREN, Capability.READ_CONTENT, Capability.URI, Capability.WRITE_CONTENT,
              Capability.GET_LAST_MODIFIED, Capability.RANDOM_ACCESS_READ } ) );

  @Test public void testFileSystem() {
    FileName fileName = mock( FileName.class );
    FileSystemOptions options = new FileSystemOptions();
    GoogleDriveFileSystem fileSystem = new GoogleDriveFileSystem( fileName, new FileSystemOptions() );
    Collection<Capability> fileSystemCapabilities = new ArrayList<>();
    fileSystem.addCapabilities( fileSystemCapabilities );
    assertTrue( capabilities.containsAll( fileSystemCapabilities ) );
  }
}
