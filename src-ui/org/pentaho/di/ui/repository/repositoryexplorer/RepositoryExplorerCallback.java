package org.pentaho.di.ui.repository.repositoryexplorer;

import org.pentaho.di.repository.RepositoryElementLocationInterface;

public interface RepositoryExplorerCallback {
  /**
   * request that specified object be opened in 'Spoon' display
   * @param object
   * @return boolean indicating if repository explorer dialog should close
   */
  boolean open(RepositoryElementLocationInterface object, String revision);

}
