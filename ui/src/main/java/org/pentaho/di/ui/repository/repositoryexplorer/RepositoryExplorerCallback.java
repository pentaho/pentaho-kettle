/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2028-08-13
 ******************************************************************************/

package org.pentaho.di.ui.repository.repositoryexplorer;

import org.pentaho.di.ui.repository.repositoryexplorer.model.UIRepositoryContent;

public interface RepositoryExplorerCallback {
  /**
   * request that specified object be opened in 'Spoon' display
   *
   * @param object
   * @return boolean indicating if repository explorer dialog should close
   */
  boolean open( UIRepositoryContent object, String revision ) throws Exception;

  /**
   * The method is called when a connection to current repository has been lost
   * @param message - error message
   * @return <code>true</code> if it is required to close the dialog and <code>false</code> otherwise
   */
  boolean error( String message ) throws Exception;

}
