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

public interface ProgressMonitorListener {
  public void beginTask( String message, int nrWorks );

  public void subTask( String message );

  public boolean isCanceled();

  public void worked( int nrWorks );

  public void done();

  public void setTaskName( String taskName );
}
