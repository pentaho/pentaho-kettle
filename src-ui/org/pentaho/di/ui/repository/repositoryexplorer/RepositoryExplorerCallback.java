package org.pentaho.di.ui.repository.repositoryexplorer;

import org.pentaho.di.ui.repository.repositoryexplorer.model.UIRepositoryContent;

public interface RepositoryExplorerCallback {
  /**
   * request that specified object be opened in 'Spoon' display
   * @param object
   * @return boolean indicating if repository explorer dialog should close
   */
  boolean open(UIRepositoryContent object, String revision);

}
