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


package org.pentaho.di.core.listeners;

import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.job.Job;
import org.pentaho.di.trans.Trans;

public class SubComponentExecutionAdapter implements SubComponentExecutionListener {

  @Override
  public void beforeTransformationExecution( Trans trans ) throws KettleException {
  }

  @Override
  public void afterTransformationExecution( Trans trans ) throws KettleException {
  }

  @Override
  public void beforeJobExecution( Job job ) throws KettleException {
  }

  @Override
  public void afterJobExecution( Job job ) throws KettleException {
  }

}
