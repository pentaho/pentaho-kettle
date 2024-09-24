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

import org.pentaho.di.repository.RepositoryMeta;

public interface RepositoryDialogInterface {
  public static enum MODE {
    ADD, EDIT
  }

  /**
   * Open the dialog
   *
   * @param mode
   *          (Add or Edit)
   * @return the description of the repository
   * @throws RepositoryAlreadyExistException
   */
  public RepositoryMeta open( final MODE mode );
}
