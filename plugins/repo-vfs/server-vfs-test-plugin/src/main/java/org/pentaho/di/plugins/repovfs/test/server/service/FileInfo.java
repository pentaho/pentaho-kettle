/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2026 by Pentaho Canada Inc. : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2030-06-15
 ******************************************************************************/
package org.pentaho.di.plugins.repovfs.test.server.service;

import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.apache.commons.vfs2.FileType;

public class FileInfo {
  public final String name;
  public final String path;
  public final boolean directory;
  public final long size;
  public final long lastModified;

  public FileInfo( FileObject obj ) throws FileSystemException {
    name = obj.getName().getBaseName();
    path = obj.getName().toString();
    directory = obj.getType() == FileType.FOLDER;
    if ( directory ) {
      this.size = -1;
    } else {
      this.size = obj.getContent().getSize();
    }
    this.lastModified = obj.getContent().getLastModifiedTime();
  }

}