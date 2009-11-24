package org.pentaho.di.repository;

import java.util.List;

public interface Directory extends RepositoryElement{
  
  public List<Directory> getChildren();

  public void setChildren(List<Directory> children);

  public void setRepository(Repository repository);
  
  public Repository getRepository();
  
}
