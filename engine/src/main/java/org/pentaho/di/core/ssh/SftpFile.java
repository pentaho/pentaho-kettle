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
