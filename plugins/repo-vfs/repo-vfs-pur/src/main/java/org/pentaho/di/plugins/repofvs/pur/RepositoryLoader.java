package org.pentaho.di.plugins.repofvs.pur;

import org.pentaho.platform.api.repository2.unified.IUnifiedRepository;

import org.apache.commons.vfs2.FileSystemOptions;

/**
 * Supplies the appropriate {@link IUnifiedRepository}
 */
public interface RepositoryLoader {

  public static class RepositoryLoadException extends Exception {
    public RepositoryLoadException( String msg ) {
      super( msg );
    }

    public RepositoryLoadException( String msg, Throwable cause ) {
      super( msg, cause );
    }
  }

  IUnifiedRepository loadRepository( FileSystemOptions opts ) throws RepositoryLoadException;
}
