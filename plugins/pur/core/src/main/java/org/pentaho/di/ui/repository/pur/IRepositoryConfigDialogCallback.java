/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2029-07-20
 ******************************************************************************/

package org.pentaho.di.ui.repository.pur;

import org.pentaho.di.repository.pur.PurRepositoryMeta;

public interface IRepositoryConfigDialogCallback {
  /**
   * On a successful configuration of a repostory, this method is invoked
   * 
   * @param repositoryMeta
   */
  void onSuccess( PurRepositoryMeta repositoryMeta );

  /**
   * On a user cancelation from the repository configuration dialog, this method is invoked
   */
  void onCancel();

  /**
   * On any error caught during the repository configuration process, this method is invoked
   * 
   * @param t
   */
  void onError( Throwable t );
}
