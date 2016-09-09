package org.pentaho.di.repository;

import org.pentaho.di.core.exception.KettleException;

/**
 * Additional methods to be added to Repository in next major revision.
 *
 */
@Deprecated
public interface RepositoryExtended extends Repository {
  /**
   * Loads the RepositoryDirectoryTree either Eagerly or Lazilly based on the value passed. This value will
   * override the default and any specified setting value for KETTLE_LAZY_REPOSITORY.
   *
   * @param eager
   * @return
   * @throws KettleException
   */
  RepositoryDirectoryInterface loadRepositoryDirectoryTree( boolean eager ) throws KettleException;
}
