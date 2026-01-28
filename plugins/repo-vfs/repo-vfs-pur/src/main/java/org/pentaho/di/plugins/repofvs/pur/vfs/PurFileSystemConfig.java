package org.pentaho.di.plugins.repofvs.pur.vfs;

import org.pentaho.di.core.bowl.Bowl;
import org.pentaho.di.core.bowl.BowlReference;

import java.util.Optional;

import org.apache.commons.vfs2.FileSystem;
import org.apache.commons.vfs2.FileSystemConfigBuilder;
import org.apache.commons.vfs2.FileSystemOptions;

/**
 * Allows setting/getting {@link PurFileSystem}-specific parameters in a {@link FileSystemOptions}
 */
public class PurFileSystemConfig extends FileSystemConfigBuilder {

  private static final String PARAM_USER = "user";
  private static final String PARAM_PASSWORD = "password";
  private static final String PARAM_REPO_NAME = "repository_name";
  private static final String PARAM_BOWL = "kettle.bowl";

  private final FileSystemOptions opts;

  public PurFileSystemConfig( FileSystemOptions opts ) {
    this.opts = opts;
  }

  @Override
  protected Class<? extends FileSystem> getConfigClass() {
    return PurFileSystem.class;
  }

  public void setUser( String user ) {
    setParam( opts, PARAM_USER, user );
  }

  public void setPassword( String pass ) {
    setParam( opts, PARAM_PASSWORD, pass );
  }

  public void setRepoName( String repoName ) {
    setParam( opts, PARAM_REPO_NAME, repoName );
  }

  public Optional<String> getUser() {
    return Optional.ofNullable( getParam( opts, PARAM_USER ) );
  }

  public Optional<String> getPass() {
    return Optional.ofNullable( getParam( opts, PARAM_PASSWORD ) );
  }

  public Optional<String> getRepoName() {
    return Optional.ofNullable( getParam( opts, PARAM_REPO_NAME ) );
  }

  public Optional<Bowl> getBowl() {
    if ( getParam( opts, PARAM_BOWL ) instanceof BowlReference bowlRef ) {
      return Optional.ofNullable( bowlRef.getBowl() );
    }
    return Optional.empty();
  }

  public boolean isRemote() {
    return getRepoName().isPresent();
  }
}
