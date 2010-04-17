package org.pentaho.di.repository;

/**
 * The RepositoryObjectInterface represents all objects that can come out of a repository, including
 * directories (RepositoryDirectoryInterface), elements such as TransMeta and JobMeta 
 * (RepositoryElementMetaInterface), and metadata about elements (RepositoryElementMetaInterface).
 * 
 * All repository objects have a name and id.
 */
public interface RepositoryObjectInterface {

  /**
   * The name of the repository object
   * 
   * @return the name of the object
   */
  public String getName();

  /**
   * The id of the object
   * 
   * @return the id of the object
   */
  public ObjectId getObjectId();
  
}
