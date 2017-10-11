/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2017 by Hitachi Vantara : http://www.pentaho.com
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

package org.pentaho.di.ui.spoon.dialog;

import java.lang.reflect.InvocationTargetException;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.swt.widgets.Shell;
import org.pentaho.di.core.ProgressMonitorAdapter;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.trans.DatabaseImpact;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.ui.core.dialog.ErrorDialog;

/**
 * Takes care of displaying a dialog that will handle the wait while where determining the impact of a transformation on
 * the used databases.
 *
 * @author Matt
 * @since 04-apr-2005
 */
public class AnalyseImpactProgressDialog {
  private static Class<?> PKG = AnalyseImpactProgressDialog.class; // for i18n purposes, needed by Translator2!!

  private Shell shell;
  private TransMeta transMeta;
  private List<DatabaseImpact> impact;
  private boolean impactHasRun;

  /**
   * Creates a new dialog that will handle the wait while determining the impact of the transformation on the databases
   * used...
   */
  public AnalyseImpactProgressDialog( Shell shell, TransMeta transMeta, List<DatabaseImpact> impact ) {
    this.shell = shell;
    this.transMeta = transMeta;
    this.impact = impact;
  }

  public boolean open() {
    IRunnableWithProgress op = new IRunnableWithProgress() {
      public void run( IProgressMonitor monitor ) throws InvocationTargetException, InterruptedException {
        try {
          impact.clear(); // Start with a clean slate!!
          transMeta.analyseImpact( impact, new ProgressMonitorAdapter( monitor ) );
          impactHasRun = true;
        } catch ( Exception e ) {
          impact.clear();
          impactHasRun = false;
          // Problem encountered generating impact list: {0}
          throw new InvocationTargetException( e, BaseMessages.getString(
            PKG, "AnalyseImpactProgressDialog.RuntimeError.UnableToAnalyzeImpact.Exception", e.toString() ) );
        }
      }
    };

    try {
      ProgressMonitorDialog pmd = new ProgressMonitorDialog( shell );
      pmd.run( true, true, op );
    } catch ( InvocationTargetException e ) {
      new ErrorDialog( shell,
        BaseMessages.getString( PKG, "AnalyseImpactProgressDialog.Dialog.UnableToAnalyzeImpact.Title" ),
        BaseMessages.getString( PKG, "AnalyseImpactProgressDialog.Dialog.UnableToAnalyzeImpact.Messages" ), e );
    } catch ( InterruptedException e ) {
      new ErrorDialog( shell,
        BaseMessages.getString( PKG, "AnalyseImpactProgressDialog.Dialog.UnableToAnalyzeImpact.Title" ),
        BaseMessages.getString( PKG, "AnalyseImpactProgressDialog.Dialog.UnableToAnalyzeImpact.Messages" ), e );
    }

    return impactHasRun;
  }
}
