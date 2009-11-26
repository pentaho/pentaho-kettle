package org.pentaho.di.repository;

import java.util.Date;

public interface RepositoryContent extends RepositoryElement {

  public Date getModifiedDate();

  public String getModifiedUser();

  public RepositoryObjectType getObjectType();

  public String getDescription();

  public String getLockMessage();
  
  public boolean isDeleted();
  
  public void setName(String name);
  
  public String getName();
  
}
