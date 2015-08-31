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

package org.pentaho.di.ui.trans.dialog;

import java.lang.reflect.InvocationTargetException;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.swt.widgets.Shell;
import org.pentaho.di.core.ProgressMonitorAdapter;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.extension.ExtensionPointHandler;
import org.pentaho.di.core.extension.KettleExtensionPoint;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.repository.KettleRepositoryLostException;
import org.pentaho.di.repository.ObjectId;
import org.pentaho.di.repository.Repository;
import org.pentaho.di.repository.RepositoryDirectoryInterface;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.ui.core.dialog.ErrorDialog;
import org.pentaho.di.ui.spoon.Spoon;
import org.pentaho.di.ui.trans.steps.missing.MissingTransDialog;

/**
 * Takes care of displaying a dialog that will handle the wait while loading a transformation...
 *
 * @author Matt
 * @since 13-mrt-2005
 */
public class TransLoadProgressDialog {
  private static Class<?> PKG = TransDialog.class; // for i18n purposes, needed by Translator2!!

  private Shell shell;
  private Repository rep;
  private String transname;
  private RepositoryDirectoryInterface repdir;
  private TransMeta transInfo;
  private ObjectId objectId;

  private String versionLabel;

  /**
   * Creates a new dialog that will handle the wait while loading a transformation...
   */
  public TransLoadProgressDialog( Shell shell, Repository rep, String transname,
    RepositoryDirectoryInterface repdir, String versionLabel ) {
    this.shell = shell;
    this.rep = rep;
    this.transname = transname;
    this.repdir = repdir;
    this.versionLabel = versionLabel;

    this.transInfo = null;
  }

  /**
   * Creates a new dialog that will handle the wait while loading a transformation...
   */
  public TransLoadProgressDialog( Shell shell, Repository rep, ObjectId objectId, String versionLabel ) {
    this.shell = shell;
    this.rep = rep;
    this.objectId = objectId;
    this.versionLabel = versionLabel;

    this.transInfo = null;
  }

  public TransMeta open() {
    IRunnableWithProgress op = new IRunnableWithProgress() {
      public void run( IProgressMonitor monitor ) throws InvocationTargetException, InterruptedException {
        Spoon spoon = Spoon.getInstance();
        try {
          // Call extension point(s) before the file has been opened
          ExtensionPointHandler.callExtensionPoint(
            spoon.getLog(),
            KettleExtensionPoint.TransBeforeOpen.id,
            ( objectId == null ) ? transname : objectId.toString() );

          if ( objectId != null ) {
            transInfo = rep.loadTransformation( objectId, versionLabel );
          } else {
            transInfo =
              rep.loadTransformation(
                transname, repdir, new ProgressMonitorAdapter( monitor ), true, versionLabel );
          }
          // Call extension point(s) now that the file has been opened
          ExtensionPointHandler.callExtensionPoint(spoon.getLog(), KettleExtensionPoint.TransAfterOpen.id, transInfo );
          if ( transInfo.hasMissingPlugins() ) {
            StepMeta stepMeta = transInfo.getStep( 0 );
            MissingTransDialog missingTransDialog =
                new MissingTransDialog( shell, transInfo.getMissingTrans(), stepMeta.getStepMetaInterface(), transInfo,
                    stepMeta.getName() );
            if ( missingTransDialog.open() == null ) {
              transInfo = null;
            }
          }
        } catch ( KettleException e ) {
          throw new InvocationTargetException( e, BaseMessages.getString(
            PKG, "TransLoadProgressDialog.Exception.ErrorLoadingTransformation" ) );
        }
      }
    };

    try {
      ProgressMonitorDialog pmd = new ProgressMonitorDialog( shell );
      pmd.run( false, false, op );
    } catch ( InvocationTargetException e ) {
      KettleRepositoryLostException krle = KettleRepositoryLostException.lookupStackStrace( e );
      if ( krle != null ) {
        throw krle;
      }
      new ErrorDialog( shell,
        BaseMessages.getString( PKG, "TransLoadProgressDialog.ErrorLoadingTransformation.DialogTitle" ),
        BaseMessages.getString( PKG, "TransLoadProgressDialog.ErrorLoadingTransformation.DialogMessage" ), e );
      transInfo = null;
    } catch ( InterruptedException e ) {
      new ErrorDialog( shell,
        BaseMessages.getString( PKG, "TransLoadProgressDialog.ErrorLoadingTransformation.DialogTitle" ),
        BaseMessages.getString( PKG, "TransLoadProgressDialog.ErrorLoadingTransformation.DialogMessage" ), e );
      transInfo = null;
    }

    return transInfo;
  }
}
