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

package org.pentaho.di.core.ssh;

import java.time.Instant;

public class SftpFile {
  private final String name;
  private final boolean directory;
  private final long size;
  private final Instant modified;

  public SftpFile( String name, boolean directory, long size, Instant modified ) {
    this.name = name;
    this.directory = directory;
    this.size = size;
    this.modified = modified;
  }

  public String getName() {
    return name;
  }

  public boolean isDirectory() {
    return directory;
  }

  public long getSize() {
    return size;
  }

  public Instant getModified() {
    return modified;
  }
}
