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


package org.pentaho.di.ui.job.dialog;

import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;
import org.pentaho.di.core.logging.LogTableInterface;
import org.pentaho.di.job.JobMeta;

public class JobDialogLogTableExtension {
  public enum Direction {
    SHOW, RETRIEVE,
  }

  public Direction direction;
  public Shell shell;
  public LogTableInterface logTable;
  public Composite wLogOptionsComposite;
  public JobMeta jobMeta;
  public ModifyListener lsMod;
  public JobDialog jobDialog;

  public JobDialogLogTableExtension( Direction direction, Shell shell, JobMeta jobMeta,
    LogTableInterface logTable, Composite wLogOptionsComposite, ModifyListener lsMod, JobDialog jobDialog ) {
    super();
    this.direction = direction;
    this.shell = shell;
    this.jobMeta = jobMeta;
    this.logTable = logTable;
    this.wLogOptionsComposite = wLogOptionsComposite;
    this.lsMod = lsMod;
    this.jobDialog = jobDialog;
  }
}
