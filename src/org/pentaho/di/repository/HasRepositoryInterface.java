package org.pentaho.di.repository;

public interface HasRepositoryInterface {
  /**
   * @return the repository
   */
  public Repository getRepository();

  /**
   * @param repository
   *          the repository to set
   */
  public void setRepository(Repository repository);

}
