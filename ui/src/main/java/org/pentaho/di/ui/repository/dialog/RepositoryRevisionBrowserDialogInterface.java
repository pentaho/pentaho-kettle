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

package org.pentaho.di.ui.repository.dialog;

public interface RepositoryRevisionBrowserDialogInterface {
  /**
   * Open the repository version browser dialog
   *
   * @return the version selected
   */
  public String open();
}
