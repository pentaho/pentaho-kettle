//CHECKSTYLE:EmptyBlock:OFF
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


package org.pentaho.di.core;

public class ProgressNullMonitorListener implements ProgressMonitorListener {

  @Override
  public void beginTask( String message, int nrWorks ) {
  }

  @Override
  public void subTask( String message ) {
  }

  @Override
  public boolean isCanceled() {
    return false;
  }

  @Override
  public void worked( int nrWorks ) {
  }

  @Override
  public void done() {
  }

  @Override
  public void setTaskName( String taskName ) {
  }
}
