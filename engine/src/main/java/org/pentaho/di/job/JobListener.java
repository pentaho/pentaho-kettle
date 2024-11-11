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

public interface JobListener {
  public void jobFinished( Job job ) throws KettleException;

  public void jobStarted( Job job ) throws KettleException;
}
