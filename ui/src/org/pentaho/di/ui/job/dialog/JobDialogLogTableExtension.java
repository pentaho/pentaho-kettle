/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2013 by Pentaho : http://www.pentaho.com
 *
 *******************************************************************************
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
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
