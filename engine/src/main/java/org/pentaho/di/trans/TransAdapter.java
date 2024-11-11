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

/**
 * Utility class to allow only certain methods of TransListener to be overridden.
 *
 * @author matt
 *
 */
public class TransAdapter implements TransListener {

  @Override public void transStarted( Trans trans ) throws KettleException {

  }

  @Override public void transActive( Trans trans ) {
  }

  @Override public void transFinished( Trans trans ) throws KettleException {
  }

}
