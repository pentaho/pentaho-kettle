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


package org.pentaho.di.ui.repository;

import org.pentaho.di.repository.Repository;

/**
 * This interface defines a Spoon Login callback.
 *
 * @author rmansoor
 *
 */
public interface ILoginCallback {

  /**
   * On a successful login to the repository, this method is invoked
   *
   * @param repository
   */
  void onSuccess( Repository repository );

  /**
   * On a user cancelation from the repository login dialog, this method is invoked
   */
  void onCancel();

  /**
   * On any error caught during the login process, this method is invoked
   *
   * @param t
   */
  void onError( Throwable t );
}
