package org.pentaho.di.repository;

import java.util.Date;

/**
 * The RepositoryElementMetaInterface is used to provide metadata about repository elements 
 * without requiring loading the entire element from the repository.  
 */
public interface RepositoryElementMetaInterface extends RepositoryObjectInterface {

  public Date getModifiedDate();

  public String getModifiedUser();

  public RepositoryObjectType getObjectType();

  public String getDescription();

  public boolean isDeleted();
  
  public void setName(String name);
 
  public RepositoryDirectoryInterface getRepositoryDirectory();
  
}
