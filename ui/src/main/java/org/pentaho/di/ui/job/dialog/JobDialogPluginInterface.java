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


package org.pentaho.di.ui.job.dialog;

import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;
import org.pentaho.di.core.logging.LogTableInterface;
import org.pentaho.di.job.JobMeta;

public interface JobDialogPluginInterface {
  public void addTab( JobMeta jobMeta, Shell shell, CTabFolder tabFolder );

  public void getData( JobMeta jobMeta );

  public void ok( JobMeta jobMeta );

  public void showLogTableOptions( JobMeta jobMeta, LogTableInterface logTable, Composite wLogOptionsComposite );
}
