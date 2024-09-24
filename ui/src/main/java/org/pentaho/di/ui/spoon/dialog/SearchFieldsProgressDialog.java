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

package org.pentaho.di.ui.spoon.dialog;

import java.lang.reflect.InvocationTargetException;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.pentaho.di.core.ProgressMonitorAdapter;
import org.pentaho.di.core.exception.KettleStepException;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.StepMeta;

/**
 *
 *
 * @author Matt
 * @since 10-mrt-2005
 */
public class SearchFieldsProgressDialog implements IRunnableWithProgress {
  private static Class<?> PKG = SearchFieldsProgressDialog.class; // for i18n purposes, needed by Translator2!!

  private StepMeta stepInfo;
  private boolean before;
  private TransMeta transMeta;
  private RowMetaInterface fields;

  public SearchFieldsProgressDialog( TransMeta transMeta, StepMeta stepMeta, boolean before ) {
    this.transMeta = transMeta;
    this.stepInfo = stepMeta;
    this.before = before;
    this.fields = null;
  }

  public void run( IProgressMonitor monitor ) throws InvocationTargetException, InterruptedException {
    int size = transMeta.findNrPrevSteps( stepInfo );

    try {
      if ( before ) {
        monitor.beginTask( BaseMessages.getString(
          PKG, "SearchFieldsProgressDialog.Dialog.SearchInputFields.Message" ), size ); // Searching
                                                                                        // for
                                                                                        // input
                                                                                        // fields...
        fields = transMeta.getPrevStepFields( stepInfo, new ProgressMonitorAdapter( monitor ) );
      } else {
        monitor.beginTask( BaseMessages.getString(
          PKG, "SearchFieldsProgressDialog.Dialog.SearchOutputFields.Message" ), size ); // Searching
                                                                                         // for
                                                                                         // output
                                                                                         // fields...
        fields = transMeta.getStepFields( stepInfo, new ProgressMonitorAdapter( monitor ) );
      }
    } catch ( KettleStepException kse ) {
      throw new InvocationTargetException( kse, BaseMessages.getString(
        PKG, "SearchFieldsProgressDialog.Log.UnableToGetFields", stepInfo.toString(), kse.getMessage() ) );
    }

    monitor.done();
  }

  /**
   * @return Returns the before.
   */
  public boolean isBefore() {
    return before;
  }

  /**
   * @param before
   *          The before to set.
   */
  public void setBefore( boolean before ) {
    this.before = before;
  }

  /**
   * @return Returns the fields.
   */
  public RowMetaInterface getFields() {
    return fields;
  }

  /**
   * @param fields
   *          The fields to set.
   */
  public void setFields( RowMetaInterface fields ) {
    this.fields = fields;
  }

  /**
   * @return Returns the stepInfo.
   */
  public StepMeta getStepInfo() {
    return stepInfo;
  }

  /**
   * @param stepInfo
   *          The stepInfo to set.
   */
  public void setStepInfo( StepMeta stepInfo ) {
    this.stepInfo = stepInfo;
  }

  /**
   * @return Returns the transMeta.
   */
  public TransMeta getTransMeta() {
    return transMeta;
  }

  /**
   * @param transMeta
   *          The transMeta to set.
   */
  public void setTransMeta( TransMeta transMeta ) {
    this.transMeta = transMeta;
  }
}
