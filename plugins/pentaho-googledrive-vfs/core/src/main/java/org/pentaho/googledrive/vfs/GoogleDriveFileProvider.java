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


package org.pentaho.googledrive.vfs;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

import org.apache.commons.vfs2.*;
import org.apache.commons.vfs2.provider.AbstractOriginatingFileProvider;

public class GoogleDriveFileProvider extends AbstractOriginatingFileProvider {

  public static final String SCHEME = "googledrive";
  public static final String DISPLAY_NAME = "Google Drive";

  protected static final Collection<Capability>
      capabilities =
      Collections.unmodifiableCollection( Arrays.asList(
          new Capability[] { Capability.CREATE, Capability.DELETE, Capability.RENAME, Capability.GET_TYPE,
              Capability.LIST_CHILDREN, Capability.READ_CONTENT, Capability.URI, Capability.WRITE_CONTENT,
              Capability.GET_LAST_MODIFIED, Capability.RANDOM_ACCESS_READ } ) );

  public FileSystem doCreateFileSystem( FileName fileName, FileSystemOptions fileSystemOptions )
      throws FileSystemException {
    return new GoogleDriveFileSystem( fileName, fileSystemOptions );
  }

  public Collection<Capability> getCapabilities() {
    return capabilities;
  }
}
