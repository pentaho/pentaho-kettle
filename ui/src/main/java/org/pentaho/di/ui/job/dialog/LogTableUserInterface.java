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



package org.pentaho.di.ui.job.dialog;

import org.eclipse.swt.widgets.Composite;
import org.pentaho.di.core.logging.LogTableInterface;

public interface LogTableUserInterface {

  public void retrieveLogTableOptions( LogTableInterface logTable );

  public void showLogTableOptions( Composite composite, LogTableInterface logTable );
}
