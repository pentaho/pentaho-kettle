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
package org.pentaho.di.ui.core.database.dialog;

import org.eclipse.core.runtime.IProgressMonitor;

public interface DatabaseInfoProgressListener {

  /**
   * Notifies that the database progress info dialog has finished
   * @param progressMonitor the progress monitor with the state of the progress
   */
  void databaseInfoProgressFinished( IProgressMonitor progressMonitor );
}
