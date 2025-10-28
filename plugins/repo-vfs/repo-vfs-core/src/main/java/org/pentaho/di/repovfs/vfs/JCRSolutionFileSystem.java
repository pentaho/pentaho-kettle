package org.pentaho.di.repovfs.vfs;

import org.apache.commons.vfs2.Capability;
import org.apache.commons.vfs2.FileName;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemConfigBuilder;
import org.apache.commons.vfs2.FileSystemException;
import org.apache.commons.vfs2.FileSystemOptions;
import org.apache.commons.vfs2.provider.AbstractFileName;
import org.apache.commons.vfs2.provider.AbstractFileSystem;

import org.pentaho.di.repovfs.cfg.JCRSolutionConfig;
import org.pentaho.di.repovfs.repo.RepositoryClient;

import java.util.Collection;

/**
 * Filesystem for the Pentaho JCR File Repository
 * Adapted from Report Designer's libpensol
 */
public class JCRSolutionFileSystem extends AbstractFileSystem {
  private RepositoryClient repoClient;
  public static final String LAST_REFRESH_TIME_ATTRIBUTE = "lastRefreshTime";

  private long refreshTime;

  public JCRSolutionFileSystem( final FileName rootName,
                                final FileSystemOptions fileSystemOptions,
                                JCRSolutionConfig config,
                                final RepositoryClient repositoryClient ) {
    super( rootName, null, fileSystemOptions );
    this.repoClient = repositoryClient;
  }

  public RepositoryClient getRepositoryClient() {
    return repoClient;
  }

  /**
   * Creates a file object.  This method is called only if the requested file is not cached.
   */
  protected FileObject createFile( final AbstractFileName name ) throws Exception {
    return new JCRSolutionFileObject( name, this, repoClient );
  }

  /**
   * Adds the capabilities of this file system.
   */
  @Override
  protected void addCapabilities( Collection<Capability> caps ) {
    caps.addAll( JCRSolutionFileProvider.capabilities );
  }

  /**
   * Retrieves the attribute with the specified name. The default implementation simply throws an exception.
   */
  public Object getAttribute( final String attrName ) throws FileSystemException {
    if ( LAST_REFRESH_TIME_ATTRIBUTE.equals( attrName ) ) {
      return Long.valueOf( refreshTime );
    }
    return null;
  }

  public static ConfigBuilder createConfigBuilder() {
    return new ConfigBuilder();
  }

  public static class ConfigBuilder extends FileSystemConfigBuilder {

    private static final class Params {
      public static final String USER = "user";
      public static final String PASS = "password";
      public static final String URL = "pentaho_url";
      public static final String TIMEOUT = "timeout";
      public static final String USE_LOCAL = "use_local_repo";
    }

    @Override
    protected Class<JCRSolutionFileSystem> getConfigClass() {
      return JCRSolutionFileSystem.class;
    }

    public void setUser( FileSystemOptions opts, String user ) {
      setParam( opts, Params.USER, user );
    }

    public void setPassword( FileSystemOptions opts, String pass ) {
      setParam( opts, Params.PASS, pass );
    }

    public void setUrl( FileSystemOptions opts, String url ) {
      setParam( opts, Params.URL, url );
    }

    public String getUser( FileSystemOptions opts ) {
      return getParam( opts, Params.USER );
    }

    public String getPass( FileSystemOptions opts ) {
      return getParam( opts, Params.PASS );
    }

    public String getUrl( FileSystemOptions opts ) {
      return getParam( opts, Params.URL );
    }

    public void setTimeOut( final FileSystemOptions opts, final int timeOut ) {
      setParam( opts, Params.TIMEOUT, timeOut );
    }

    public int getTimeOut( final FileSystemOptions opts ) {
      Integer timeout = getInteger( opts, Params.TIMEOUT );
      return timeout != null ? timeout : 0;
    }

    public void setUseLocalRepo( FileSystemOptions opts, boolean useLocalRepo ) {
      setParam( opts, Params.USE_LOCAL, useLocalRepo );
    }

    public boolean getUseLocalRepo( FileSystemOptions opts ) {
      return getBoolean( opts, Params.USE_LOCAL );
    }
  }
}
