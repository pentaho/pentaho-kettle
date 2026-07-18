/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 - 2026 by Pentaho Canada Inc. : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2030-06-15
 ******************************************************************************/



package org.pentaho.di.job;

import org.pentaho.di.core.exception.KettleException;

public interface JobListener {
  public void jobFinished( Job job ) throws KettleException;

  public void jobStarted( Job job ) throws KettleException;
}
