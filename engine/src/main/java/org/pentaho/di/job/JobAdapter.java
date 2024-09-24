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

package org.pentaho.di.job;

import org.pentaho.di.core.exception.KettleException;

/**
 * Utility class to allow only certain methods of JobListener to be overridden.
 *
 * @author Marc
 *
 */

public class JobAdapter implements JobListener {

  @Override
  public void jobFinished( Job job ) throws KettleException {
    // NoOp

  }

  @Override
  public void jobStarted( Job job ) throws KettleException {
    // NoOp

  }

}
