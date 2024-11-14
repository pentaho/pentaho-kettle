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


package org.pentaho.di.trans;

import org.pentaho.di.core.exception.KettleException;

public interface TransListener {

  /**
   * This transformation started
   *
   * @param trans
   * @throws KettleException
   */
  void transStarted( Trans trans ) throws KettleException;

  /**
   * This transformation went from an in-active to an active state.
   *
   * @param trans
   * @throws KettleException
   */
  public void transActive( Trans trans );

  /**
   * The transformation has finished.
   *
   * @param trans
   * @throws KettleException
   */
  public void transFinished( Trans trans ) throws KettleException;
}
