package org.pentaho.di.repository;

import java.util.List;

public interface Directory extends RepositoryElement{
  
  public List<Directory> getChildren();

  public void setChildren(List<Directory> children);

  //public void setRepository(Repository repository);
  
  //public Repository getRepository();
  
  /**
   * If true, this directory should be shown in UIs. Not necessarily persisted. Each repository implementation decides 
   * whether to mark each directory as visible.
   */
  public boolean isVisible();
  
}
